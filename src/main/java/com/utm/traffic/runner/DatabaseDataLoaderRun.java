package com.utm.traffic.runner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.utm.traffic.constant.RedisKeyConstant;
import com.utm.traffic.entity.BaseMonitorBbox;
import com.utm.traffic.entity.BaseMonitorPoint;
import com.utm.traffic.entity.SysApiKey;
import com.utm.traffic.entity.SysUserAgent;
import com.utm.traffic.mapper.BaseMonitorBboxMapper;
import com.utm.traffic.mapper.BaseMonitorPointMapper;
import com.utm.traffic.mapper.SysApiKeyMapper;
import com.utm.traffic.mapper.SysUserAgentMapper;
import com.utm.traffic.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

/**
 * 启动缓存加载任务（监控点、API Key、BBOX、User-Agent、心跳）
 */
@Slf4j
@Order(1)
@Component
public class DatabaseDataLoaderRun implements CommandLineRunner {

    @Autowired
    private BaseMonitorPointMapper baseMonitorPointMapper;
    @Autowired
    private SysApiKeyMapper sysApiKeyMapper;
    @Autowired
    private BaseMonitorBboxMapper baseMonitorBboxMapper;
    @Autowired
    private SysUserAgentMapper sysUserAgentMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void run(String... args) throws Exception {

        log.info("===== 启动缓存初始化任务开始 =====");
        // ① 启动初始化心跳（必须优先）
        initHeartbeat();
        // ② TOMTOM 数据加载
        if (!loadMonitorPointsToRedis() || !loadApiKeysToRedis()) {
            log.error("启动加载 TOMTOM 缓存失败，应用终止");
            throw new IllegalStateException("TOMTOM 缓存加载失败，服务无法运行");
        }
        log.info("===== TOMTOM 缓存加载成功 =====");
        // ③ HERE BBOX 数据加载
        if (!loadHereBboxToRedis()) {
            log.error("启动加载 HERE 缓存失败，应用终止");
            throw new IllegalStateException("HERE 缓存加载失败，服务无法运行");
        }
        log.info("===== HERE 缓存加载成功 =====");
        // 4. User-Agent 数据加载
        if (!loadUserAgentToRedis()) {
            log.error("启动加载 User-Agent 缓存失败，应用终止");
            throw new IllegalStateException("User-Agent 缓存加载失败，服务无法运行");
        }
        log.info("===== User-Agent 缓存加载成功 =====");
        log.info("===== 启动缓存初始化任务全部完成 =====");
    }

    //  启动初始化心跳（防止邮件监控误报）
    private void initHeartbeat() {
        long now = System.currentTimeMillis();
        redisUtil.set(RedisKeyConstant.SERVICE_LAST_ALIVE, now);
        log.info("启动初始化心跳成功：SERVICE_LAST_ALIVE = {}", now);
    }

    //  加载监控点 BaseMonitorPoint
    private boolean loadMonitorPointsToRedis() {
        try {
            redisUtil.delete(RedisKeyConstant.MONITOR_POINT_KEY);

            List<BaseMonitorPoint> list = baseMonitorPointMapper.selectList(null);

            if (list == null || list.isEmpty()) {
                log.error("监控点表 base_monitor_point 无数据");
                return false;
            }

            redisUtil.set(RedisKeyConstant.MONITOR_POINT_KEY, JSON.toJSONString(list));
            log.info("监控点加载到 Redis 完成，共 {} 条", list.size());
            return true;

        } catch (Exception e) {
            log.error("监控点缓存加载异常", e);
            return false;
        }
    }

    // ======================================================
    //  加载 API Keys
    // ======================================================
    private boolean loadApiKeysToRedis() {
        try {
            redisUtil.delete(RedisKeyConstant.SYS_API_KEYS_KEY);

            List<SysApiKey> list = sysApiKeyMapper.selectList(null);
            if (list == null || list.isEmpty()) {
                log.error("API 密钥表 sys_api_keys 无数据");
                return false;
            }

            redisUtil.set(RedisKeyConstant.SYS_API_KEYS_KEY, JSON.toJSONString(list));
            log.info("API Keys 加载到 Redis 完成，共 {} 条", list.size());
            return true;

        } catch (Exception e) {
            log.error("API Keys 缓存加载异常", e);
            return false;
        }
    }

    // ======================================================
    //  加载 HERE BBOX
    // ======================================================
    private boolean loadHereBboxToRedis() {
        try {
            redisUtil.delete(RedisKeyConstant.BASE_BBOXES_KEY);

            List<BaseMonitorBbox> list = baseMonitorBboxMapper.selectList(null);
            if (list == null || list.isEmpty()) {
                log.error("HERE BBOX 表 base_monitor_bboxes 无数据");
                return false;
            }

            redisUtil.set(RedisKeyConstant.BASE_BBOXES_KEY, JSON.toJSONString(list));
            log.info("HERE BBOX 加载到 Redis 完成，共 {} 条", list.size());
            return true;

        } catch (Exception e) {
            log.error("HERE BBOX 缓存加载异常", e);
            return false;
        }
    }

    // ======================================================
    //  加载 User-Agent
    // ======================================================
    private boolean loadUserAgentToRedis() {
        try {
            redisUtil.delete(RedisKeyConstant.USER_AHENt_KEY);

            List<SysUserAgent> list = sysUserAgentMapper.selectList(null);
            if (list == null || list.isEmpty()) {
                log.error("User-Agent 表 sys_user_agent 无数据");
                return false;
            }

            redisUtil.set(RedisKeyConstant.USER_AHENt_KEY, JSON.toJSONString(list));
            log.info("User-Agent 加载到 Redis 完成，共 {} 条", list.size());
            return true;

        } catch (Exception e) {
            log.error("User-Agent 缓存加载异常", e);
            return false;
        }
    }
}
