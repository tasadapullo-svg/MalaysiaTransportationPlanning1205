package com.utm.traffic.controller;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hz.utils.UUIDUtils;
import com.utm.traffic.constant.HereCollectorTask;
import com.utm.traffic.constant.RedisKeyConstant;
import com.utm.traffic.entity.BaseMonitorBbox;
import com.utm.traffic.entity.SysJobLog;
import com.utm.traffic.entity.SysApiKey;
import com.utm.traffic.mapper.SysJobLogMapper;
import com.utm.traffic.mapper.SysApiKeyMapper;
import com.utm.traffic.mapper.LogKeyUsageMapper;
import com.utm.traffic.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class HereController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private HereCollectorTask hereCollectorTask;

    @Autowired
    private SysJobLogMapper jobLogMapper;

    @Autowired
    private LogKeyUsageMapper logKeyUsageMapper;

    @Autowired
    private SysApiKeyMapper apiKeyMapper;


    /**
     * ============================
     * HERE 主定时任务：每 30 分钟执行
     * ============================
     */
//    @Scheduled(cron = "0 0/30 * * * ?")
    public void HereTask() {
        SysJobLog jobLog = createJobLog("HERE-30MIN-TASK");
        long startTime = System.currentTimeMillis();

        try {
            log.warn("HERE 主定时任务：每 30 分钟执行开始！");
            //项目启动
            boolean result = runHereTask(jobLog);

            updateJobStatus(jobLog, result);
            if (result) updateHeartbeat();

            log.warn("===== HERE 定时任务结束，状态={}，耗时={}ms =====",
                    jobLog.getStatus(),
                    System.currentTimeMillis() - startTime);

        } catch (Exception e) {

            failJob(jobLog, e.getMessage());
            log.error("===== HERE 定时任务执行异常 =====", e);
        }
    }



    /**
     * ============================
     * 任务主流程
     * ============================
     */
    private boolean runHereTask(SysJobLog jobLog) {

        try {
            // 1. 读取 HERE 区域
            List<BaseMonitorBbox> bboxList = fetchBboxList(jobLog);
            if (bboxList == null) return false;

            log.info("读取到 HERE 区域数量={}", bboxList.size());

            // 2. 读取 API Keys
            List<SysApiKey> keyList = fetchApiKeys(jobLog);
            if (keyList == null) return false;

            log.info("读取到 HERE Key 数量={}", keyList.size());

            // 3. KEY 请求次数 +1
            addRequestCount(keyList);

            // 4. 调用 HERE API
            String batchId = UUIDUtils.getUUID();
            boolean ok = hereCollectorTask.RequestHere(bboxList, batchId);

            if (!ok) {
                addErrorCount(keyList);
                jobLog.setErrorMessage("HERE API 返回失败");
                return false;
            }

            log.warn("HERE 批次 {} 采集成功", batchId);
            jobLog.setSuccessPoints(bboxList.size());

            return true;

        } catch (Exception e) {
            jobLog.setErrorMessage(e.getMessage());
            log.error("HERE 主任务异常：", e);
            return false;
        }
    }


    // ========================================================================
    //                             抽取公共方法
    // ========================================================================

    /** 创建任务日志（开始） */
    private SysJobLog createJobLog(String jobName) {
        SysJobLog log = new SysJobLog();
        log.setUuid(UUID.randomUUID().toString());
        log.setJobName(jobName);
        log.setStartTime(new Date());
        log.setStatus("RUNNING");
        log.setCreateTime(new Date());
        jobLogMapper.insert(log);
        return log;
    }

    /** 更新任务状态 */
    private void updateJobStatus(SysJobLog jobLog, boolean success) {
        jobLog.setEndTime(new Date());
        jobLog.setStatus(success ? "SUCCESS" : "FAILED");
        jobLogMapper.updateById(jobLog);
    }

    /** 任务失败写错误信息 */
    private void failJob(SysJobLog jobLog, String error) {
        jobLog.setEndTime(new Date());
        jobLog.setStatus("FAILED");
        jobLog.setErrorMessage(error);
        jobLogMapper.updateById(jobLog);
    }

    /** 刷新心跳 */
    private void updateHeartbeat() {
        redisUtil.set("SERVICE_LAST_ALIVE", System.currentTimeMillis());
        log.info("已更新 SERVICE_LAST_ALIVE 心跳（HERE 任务成功）");
    }

    /** 从 Redis 读取 HERE 区域 */
    private List<BaseMonitorBbox> fetchBboxList(SysJobLog jobLog) {

        String json = redisUtil.getDataByType(RedisKeyConstant.BASE_BBOXES_KEY);

        if (StringUtils.isEmpty(json)) {
            jobLog.setErrorMessage("HERE 区域数据为空");
            return null;
        }

        List<BaseMonitorBbox> list = JSONArray.parseArray(json, BaseMonitorBbox.class);
        if (list == null || list.isEmpty()) {
            jobLog.setErrorMessage("HERE 区域解析为空");
            return null;
        }

        return list;
    }

    /** 读取 API Keys */
    private List<SysApiKey> fetchApiKeys(SysJobLog jobLog) {

        String json = redisUtil.getDataByType(RedisKeyConstant.SYS_API_KEYS_KEY);

        if (StringUtils.isEmpty(json)) {
            jobLog.setErrorMessage("API Key 数据为空");
            return null;
        }

        List<SysApiKey> list = JSONArray.parseArray(json, SysApiKey.class);
        if (list == null || list.isEmpty()) {
            jobLog.setErrorMessage("API Key 解析为空");
            return null;
        }

        return list;
    }

    /** 累加请求次数 */
    private void addRequestCount(List<SysApiKey> keys) {
        Date now = new Date();
        for (SysApiKey key : keys) {
            logKeyUsageMapper.addRequestCount(key.getUuid(), now, now);
        }
    }

    /** 累加错误次数 */
    private void addErrorCount(List<SysApiKey> keys) {
        Date now = new Date();
        for (SysApiKey key : keys) {
            logKeyUsageMapper.addErrorCount(key.getUuid(), now, now);
        }
    }

}
