package com.utm.traffic.utils;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hz.utils.UUIDUtils;
import com.utm.traffic.constant.RedisKeyConstant;
import com.utm.traffic.entity.BaseMonitorPoint;
import com.utm.traffic.entity.RedisInternalCombination;
import com.utm.traffic.entity.SysApiKey;
import com.utm.traffic.entity.SysUserAgent;
import com.utm.traffic.mapper.SysUserAgentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/***
 * 生成随机数分摊到每一个key中的工具类
 */
@Slf4j
@Component
public class PointAllocationUtil {


    /**
     * 核心分配方法
     *
     * @param sysApiKeyList        5个Key的列表（如Redis Key）
     * @param baseMonitorPointList 65个点位监控的列表
     * @return 每个Key对应随机分配的点位列表，类型为Map<String, List<String>>
     */
    public static List<RedisInternalCombination> distributePointsToApiKeys(
            List<SysApiKey> sysApiKeyList,
            List<BaseMonitorPoint> baseMonitorPointList) {
//        log.info("sysApiKeyList->{}", sysApiKeyList);
        // 1. 参数校验（避免空指针或无效输入）
        if (sysApiKeyList == null || sysApiKeyList.isEmpty()) {
            throw new IllegalArgumentException("SysApiKey列表不能为空！");
        }
        if (baseMonitorPointList == null || baseMonitorPointList.isEmpty()) {
            throw new IllegalArgumentException("BaseMonitorPoint列表不能为空！");
        }

        // 2. 打乱点位列表（保证随机性，Fisher-Yates洗牌算法，不修改原列表）
        List<BaseMonitorPoint> shuffledPoints = new ArrayList<>(baseMonitorPointList);
        Collections.shuffle(shuffledPoints, new Random());

        // 3. 初始化：为每个Key生成专属UUID，并初始化结果Map
        Map<String, String> keyUuidMap = new HashMap<>(); // Key: apiKey, Value: 专属UUID
        Map<String, List<RedisInternalCombination>> allocationResult = new LinkedHashMap<>();

        sysApiKeyList.forEach(s -> {
            String apiKeyStr = s.getApiKey();
            keyUuidMap.put(apiKeyStr, UUIDUtils.getUUID()); // 每个Key生成一个UUID
            allocationResult.put(apiKeyStr, new ArrayList<>());
        });

        // 4. 轮询分配点位，每个Key下的点位共用专属UUID
        int apiKeySize = sysApiKeyList.size();
        for (int i = 0; i < shuffledPoints.size(); i++) {
            // 获取当前分配的Key
            SysApiKey targetApiKey = sysApiKeyList.get(i % apiKeySize);
            String targetApiKeyStr = targetApiKey.getApiKey();
//            log.info("targetApiKeyStr:{}", targetApiKeyStr);
            // 获取当前点位
            BaseMonitorPoint point = shuffledPoints.get(i);
            // 封装RedisInternalCombination（使用Key专属UUID）
            RedisInternalCombination combination = new RedisInternalCombination();
            if (targetApiKey.getPlatform().equals("TOMTOM")) {
                combination.setKeyNumber(targetApiKeyStr);
                combination.setLatitude(point.getLatitude());
                combination.setLongitude(point.getLongitude());
                // 添加到对应Key的列表中
                allocationResult.get(targetApiKeyStr).add(combination);
            }
        }

        // 6. 将Map中的所有List合并为一个总List返回
        List<RedisInternalCombination> totalResult = new ArrayList<>();
        allocationResult.values().forEach(totalResult::addAll);
        //一批只有一个编号
        String PdUuid = UUIDUtils.getUUID();
        totalResult.stream().forEach(t -> {
            t.setPdUuid(PdUuid);
        });
//        log.info("totalResult:{}", totalResult);
        return totalResult;
    }



}
