package com.utm.traffic.controller;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.utm.traffic.constant.RedisKeyConstant;
import com.utm.traffic.constant.TomTomCollectorTask;
import com.utm.traffic.entity.BaseMonitorPoint;
import com.utm.traffic.entity.RedisInternalCombination;
import com.utm.traffic.entity.SysApiKey;
import com.utm.traffic.entity.SysJobLog;
import com.utm.traffic.mapper.LogKeyUsageMapper;
import com.utm.traffic.mapper.SysApiKeyMapper;
import com.utm.traffic.mapper.SysJobLogMapper;
import com.utm.traffic.service.IRedisInternalCombinationService;
import com.utm.traffic.utils.PointAllocationUtil;
import com.utm.traffic.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TomTomController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IRedisInternalCombinationService combinationService;

    @Autowired
    private TomTomCollectorTask tomTomCollectorTask;

    @Autowired
    private SysJobLogMapper jobLogMapper;

    @Autowired
    private LogKeyUsageMapper logKeyUsageMapper;

    @Autowired
    private SysApiKeyMapper apiKeyMapper;


    /**
     * ============================
     *  TomTom 主定时任务：每 15 分钟执行
     * ============================
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    public void scheduledTomTomTask() {

        SysJobLog jobLog = createJobLog("TOMTOM-15MIN-TASK");
        long startTime = System.currentTimeMillis();

        try {
            log.warn("TomTom 主定时任务：每 15 分钟执行 开始！");
            boolean result = runTomTomTask(jobLog);
            updateJobStatus(jobLog, result);
            if (result) updateHeartbeat();

            log.warn("===== TomTom 定时任务结束，状态={}，耗时={}ms =====",
                    jobLog.getStatus(),
                    System.currentTimeMillis() - startTime
            );

        } catch (Exception e) {

            // 捕获无法预期的异常
            failJob(jobLog, e.getMessage());
            log.error("===== TomTom 定时任务执行异常 =====", e);
        }
    }


    /**
     * =============================
     * 任务主流程（读取 Redis → 执行业务）
     * =============================
     */
    private boolean runTomTomTask(SysJobLog jobLog) {

        try {
            // 读取监控点
            List<BaseMonitorPoint> pointList = fetchMonitorPoints(jobLog);
            if (pointList == null) return false;

            jobLog.setTotalPoints(pointList.size());
            log.info("监控点数量={}", pointList.size());

            // 读取 API Keys
            List<SysApiKey> apiKeyList = fetchApiKeys(jobLog);
            if (apiKeyList == null) return false;

            log.info("API Key 数量={}", apiKeyList.size());

            // 记录 KEY 请求数
            addRequestCount(apiKeyList);

            // 执行核心任务
            boolean ok = executeTomTomCore(apiKeyList, pointList, jobLog);
            if (!ok) {
                addErrorCount(apiKeyList);
                jobLog.setErrorMessage("TomTom 核心任务失败");
            }

            return ok;

        } catch (Exception e) {
            jobLog.setErrorMessage(e.getMessage());
            log.error("主任务执行异常: ", e);
            return false;
        }
    }


    /**
     * ============================
     * 核心业务：点位分配 → 保存数据库 → 调接口
     * ============================
     */
    private boolean executeTomTomCore(
            List<SysApiKey> apiKeyList,
            List<BaseMonitorPoint> pointList,
            SysJobLog jobLog
    ) {

        try {
            // 1. 点位分配
            List<RedisInternalCombination> combinationList =
                    PointAllocationUtil.distributePointsToApiKeys(apiKeyList, pointList);
            log.warn("TOMTOM点位分配结束");
            if (combinationList == null || combinationList.isEmpty()) {
                jobLog.setErrorMessage("点位分配为空");
                return false;
            }
            log.info("点位分配数量={}", combinationList.size());

            // 2. 数据库存储
            List<RedisInternalCombination> savedList =
                    combinationService.saveRedisInternalCombination(combinationList);
            log.warn("TOMTOM分配点位存储结束");
            jobLog.setSuccessPoints(savedList.size());
            log.warn("TOMTOM分配点位存储保存点位数量={}", savedList.size());
            // 3. 调用 TomTom 接口
            boolean result = tomTomCollectorTask.TomTomInterface(savedList);
            log.warn("TomTom 批量任务拉取结束！");
            return result;
        } catch (Exception e) {
            jobLog.setErrorMessage(e.getMessage());
            log.error("TomTom 核心业务异常：", e);
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
        log.info("已更新 SERVICE_LAST_ALIVE 心跳（任务成功）");
    }

    /** 读取监控点（带校验） */
    private List<BaseMonitorPoint> fetchMonitorPoints(SysJobLog jobLog) {

        String json = redisUtil.getDataByType(RedisKeyConstant.MONITOR_POINT_KEY);

        if (StringUtils.isEmpty(json)) {
            jobLog.setErrorMessage("监控点数据为空");
            return null;
        }

        List<BaseMonitorPoint> list = JSONArray.parseArray(json, BaseMonitorPoint.class);
        if (list == null || list.isEmpty()) {
            jobLog.setErrorMessage("监控点解析为空");
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
