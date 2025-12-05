package com.utm.traffic.constant;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONException;
import com.utm.traffic.entity.BaseMonitorBbox;
import com.utm.traffic.exception.HereApiException;
import com.utm.traffic.service.IBaseMonitorBboxService;
import com.utm.traffic.utils.SysUserAgentRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.zip.InflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * HERE接口类（适配Java 8）
 */
@Slf4j
@Component
public class HereCollectorTask {
    // 随机数生成器（用于8-10秒延迟）
    private static final Random RANDOM = new Random();

    @Autowired
    private IBaseMonitorBboxService iBaseMonitorBboxService;
    @Autowired
    private SysUserAgentRedisUtil sysUserAgentRedisUtil;

    /**
     * 调用Here接口获取交通数据（JSON格式）
     * 每次请求完成后延迟8-10秒
     */
    public boolean RequestHere(List<BaseMonitorBbox> baseMonitorBboxList, String batchId) {
        if (baseMonitorBboxList == null || baseMonitorBboxList.isEmpty()) {
            log.error("BBOX列表为空，无需执行HERE接口请求");
            throw new IllegalArgumentException("BBOX列表为空，无需执行HERE接口请求");
        }
        log.info("here1.开始执行HERE接口请求，共{}个BBOX待处理", baseMonitorBboxList.size());
        for (BaseMonitorBbox bbox : baseMonitorBboxList) {
            try {
                // 1. 校验BBOX和apikey（queryString）非空
                if (bbox == null || StringUtils.isEmpty(bbox.getBbox())
                        || StringUtils.isEmpty(bbox.getQueryString())) {
                    throw new IllegalArgumentException("BBOX数据不完整");
                }
                // 2. 调用HERE接口（queryString作为apikey）
                JSONObject jsonHere = HereInterface(bbox.getBbox(), bbox.getQueryString());
                log.info("here2.here请求接口结束->{}",jsonHere.toJSONString());
                // 3. 保存数据
                iBaseMonitorBboxService.saveHereData(jsonHere,batchId,bbox.getName());
                log.info("here3.here接口保存结束->{}",bbox.getName());
                // 4. 每次请求完成后延迟8-10秒
                long delayTime = RANDOM.nextInt(2000) + 8000;
                log.warn("BBOX: {} 请求完成，延迟{}ms执行下一个请求", bbox.getBbox(), delayTime);
                Thread.sleep(delayTime);
            } catch (InterruptedException e) {
                log.error("请求延迟时线程被中断", e);
                Thread.currentThread().interrupt(); // 恢复中断标记
            } catch (HereApiException e) {
                log.error("HERE API请求异常（响应码：{}）：{}", e.getResponseCode(), e.getMessage(), e);
                // 异常时延迟5秒，避免频繁失败
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                log.error("BBOX: {} 处理异常", bbox.getBbox(), e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.info("4.HERE接口请求全部执行完毕!");
        return true;
    }


    /**
     * 核心请求方法：基于HttpURLConnection实现HERE接口调用（适配Java 8）
     *
     * @param bbox        BBOX字符串（格式：minLon,minLat,maxLon,maxLat）
     * @param queryString apikey（作为apikey参数值）
     * @return 接口返回的JSON对象（仅200响应返回有效对象）
     * @throws HereApiException 非200响应时抛出
     */
    public JSONObject HereInterface(String bbox, String queryString) throws HereApiException {
        HttpURLConnection conn = null;
        BufferedReader br = null;
        InputStream inputStream = null;
        InputStream decompressedStream = null;
        try {
            // 1. 拼装完整请求URL
            String urlStr = String.format("%s?in=bbox:%s&locationReferencing=shape&apikey=%s",
                    RedisKeyConstant.HERE_BASE_URL,
                    bbox,
                    queryString);
            log.info("HERE请求URL: {}", urlStr);

            // 2. 初始化连接
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();

            // 3. 设置请求头（Java 8适配：去掉br编码）
            conn.setRequestMethod("GET");
            //修改User-Agent 做成动态
            String sysUserAgentRedis = sysUserAgentRedisUtil.getSysUserAgentRedis();
            log.info("HERE-sysUserAgentRedis-User-Agent->{}",sysUserAgentRedis);
            conn.setRequestProperty("User-Agent", sysUserAgentRedis);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate"); // 仅保留Java 8支持的压缩类型
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Host", "data.traffic.hereapi.com");

            // 4. 设置超时时间
            conn.setConnectTimeout(8000); // 连接超时8秒
            conn.setReadTimeout(8000);    // 读取超时8秒

            // 5. 获取响应码
            int responseCode = conn.getResponseCode();
            log.info("HERE响应码: {} (BBOX: {})", responseCode, bbox);

            // 6. 获取响应流（正常流/错误流）
            inputStream = responseCode == HttpURLConnection.HTTP_OK
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (inputStream == null) {
                String errorMsg = String.format("响应流为空，响应码：%d (BBOX: %s)", responseCode, bbox);
                throw new HereApiException(responseCode, "", errorMsg);
            }

            // ========== 关键：Java 8解压处理（仅gzip/deflate） ==========
            decompressedStream = getDecompressedStreamForJava8(conn, inputStream);

            // 7. 读取响应内容（使用解压后的流）
            br = new BufferedReader(new InputStreamReader(decompressedStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String responseContent = sb.toString();
            log.debug("HERE响应内容 (BBOX: {}): {}", bbox, responseContent);

            // 8. 非200响应处理
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String errorMsg = String.format("HERE接口请求失败 (BBOX: %s)", bbox);
                throw new HereApiException(responseCode, responseContent, errorMsg);
            }

            // 9. 解析JSON响应
            try {
                return JSONObject.parseObject(responseContent);
            } catch (JSONException e) {
                log.error("HERE响应内容JSON解析失败 (BBOX: {})，响应内容：{}", bbox, responseContent, e);
                throw new HereApiException(500, responseContent, "HERE响应JSON解析失败");
            }

        } catch (IOException e) {
            log.error("HERE接口网络IO异常 (BBOX: {})", bbox, e);
            throw new HereApiException(503, e.getMessage(), "HERE接口网络请求异常");
        } finally {
            // 10. 关闭资源（按顺序关闭，避免泄漏）
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error("BufferedReader关闭异常", e);
                }
            }
            if (decompressedStream != null) {
                try {
                    decompressedStream.close();
                } catch (IOException e) {
                    log.error("解压流关闭异常", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("原始输入流关闭异常", e);
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Java 8专属解压方法：仅支持gzip/deflate（无Brotli）
     *
     * @param conn        HTTP连接
     * @param inputStream 原始输入流
     * @return 解压后的输入流
     * @throws IOException 解压失败或不支持的编码
     */
    private InputStream getDecompressedStreamForJava8(HttpURLConnection conn, InputStream inputStream) throws IOException {
        String encoding = conn.getHeaderField("Content-Encoding");
        if (encoding == null || encoding.isEmpty()) {
            return inputStream; // 无压缩，直接返回
        }

        switch (encoding.toLowerCase()) {
            case "gzip":
                log.debug("响应使用Gzip压缩，开始解压（Java 8）");
                return new GZIPInputStream(inputStream);
            case "deflate":
                log.debug("响应使用Deflate压缩，开始解压（Java 8）");
                return new InflaterInputStream(inputStream);
            default:
                throw new IOException("Java 8不支持的Content-Encoding: " + encoding);
        }
    }
}