package com.utm.traffic.constant;

import com.alibaba.fastjson.JSONObject;
import com.utm.traffic.entity.RedisInternalCombination;
import com.utm.traffic.exception.TomTomTooManyRequestsException;
import com.utm.traffic.mapper.SysUserAgentMapper;
import com.utm.traffic.service.IDataTomtomCoordinateService;
import com.utm.traffic.utils.PointAllocationUtil;
import com.utm.traffic.utils.RedisUtil;
import com.utm.traffic.utils.SysUserAgentRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

/**
 * TOMTOM接口类
 */
@Slf4j
@Component
public class TomTomCollectorTask {
    /**
     * 解析接口参数和调用定时
     */
    @Autowired
    private IDataTomtomCoordinateService iDataTomtomCoordinateService;

    @Autowired
    private SysUserAgentRedisUtil sysUserAgentRedisUtil;


    public boolean TomTomInterface(List<RedisInternalCombination> internalCombinationList) {
        // 初始化随机数生成器（生成3000-5000毫秒的随机等待时间）
        Random random = new Random();
        // 传入带有参数的集合
        for (RedisInternalCombination i : internalCombinationList) {
            JSONObject JsonRequestTomTom = RequestTomTom(i.getLatitude(), i.getLongitude(), i.getKeyNumber());
            log.info("1.TOMTOM接口返回成功返回值是->{}", JsonRequestTomTom.toJSONString());
            // 数据保存  data_redis_internal_combination uuid一个查询点位接口返回多条数据但是还是那个点位经纬度
            iDataTomtomCoordinateService.saveTomTomData(JsonRequestTomTom, i.getUuid());
            log.warn("2.TomTom一个点位保存结束批号是->{}", i.getPdUuid());
            try {
                // 生成3-5秒的随机休眠时间（3000毫秒=3秒，5000毫秒=5秒）
                long waitTime = random.nextInt(2000) + 3000; // nextInt(2000)生成0-2000，加3000后是3000-5000
                Thread.sleep(waitTime); // 休眠指定毫秒数
            } catch (InterruptedException e) {
                // 处理线程中断异常（可选：恢复中断状态或记录日志）
                Thread.currentThread().interrupt(); // 恢复中断标记
                log.warn("休眠被中断", e);
            }
            log.warn("3.休眠结束TomTom开始下一个点位拉取");
        }
        return true;
    }


    /**
     * 调用TomTom接口获取交通数据（JSON格式）
     *
     * @param lat    纬度
     * @param lon    经度
     * @param keyApi API密钥
     * @return 交通数据JSON对象
     * @throws TomTomTooManyRequestsException 请求过于频繁（429）
     */
    public JSONObject RequestTomTom(BigDecimal lat, BigDecimal lon, String keyApi) {
        HttpURLConnection conn = null;
        BufferedReader br = null;

        try {
            String urlStr = RedisKeyConstant.TOMTOM_ENDPOINT + "?point=" + lat + "," + lon + "&key=" + keyApi;
            log.info("TomTom请求URL: {}", urlStr);

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();

            // 设置请求头
            conn.setRequestMethod("GET");
            //修改User-Agent 做成动态
            String sysUserAgentRedis = sysUserAgentRedisUtil.getSysUserAgentRedis();
            log.info("TOMTOM-sysUserAgentRedis-User-Agent->{}", sysUserAgentRedis);
            conn.setRequestProperty("User-Agent", sysUserAgentRedis);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            // 检查响应码
            int responseCode = conn.getResponseCode();
            log.info("TomTom响应码: {}", responseCode);

            // 根据响应码处理不同流（正常流/错误流）
            InputStream inputStream = responseCode == HttpURLConnection.HTTP_OK
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (inputStream == null) {
                throw new IOException("响应流为空，响应码：" + responseCode);
            }

            // 读取响应内容
            br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String responseContent = sb.toString();
//            log.info("TomTom响应内容: {}", responseContent);

            // 处理429异常
            if (responseCode == 429) {
                JSONObject errorJson = JSONObject.parseObject(responseContent);
                String errorMsg = errorJson.getString("error") != null
                        ? errorJson.getString("error")
                        : "请求过于频繁（429）";
                // 提取Retry-After头（可选：接口返回的重试等待时间）
                int retryAfter = conn.getHeaderFieldInt("Retry-After", 60); // 默认60秒
                throw new TomTomTooManyRequestsException(errorMsg + "，建议等待" + retryAfter + "秒后重试", retryAfter);
            }

            // 处理其他非200响应
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("接口请求失败，响应码：" + responseCode + "，响应内容：" + responseContent);
            }

            // 解析正常JSON返回
//            log.info("TomTom<接口查询出的解析参数>: {}", responseContent);
            return JSONObject.parseObject(responseContent);

        } catch (TomTomTooManyRequestsException e) {
            // 直接抛出自定义异常
            throw e;
        } catch (Exception e) {
            log.error("TomTom请求失败 lat={}, lon={}", lat, lon, e);
            throw new RuntimeException("TomTom接口调用异常", e); // 包装其他异常
        } finally {
            // 关闭资源
            try {
                if (br != null) br.close();
                if (conn != null) conn.disconnect();
            } catch (IOException e) {
                log.error("资源关闭失败", e);
            }
        }
    }


}
