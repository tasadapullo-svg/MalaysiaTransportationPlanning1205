package com.utm.traffic.constant;

/**
 * Redis 键常量类 全局常量
 */
public class RedisKeyConstant {
    // 监控点数据键
    public static final String MONITOR_POINT_KEY = "LaLoDate";
    // API密钥数据键
    public static final String SYS_API_KEYS_KEY = "SysApiKeys";
    //here 点位和密钥
    public static final String BASE_BBOXES_KEY = "BboxsApiKeys";
    //UserAgent
    public static final String USER_AHENt_KEY = "UserAgentKeys";
    //心跳
    public static final String SERVICE_LAST_ALIVE = "serviceLastAlive";
    //TOMTOM
    public static final String TOMTOM_ENDPOINT =
            "https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json";
    // HERE API基础URL（固定）
    public static final String HERE_BASE_URL = "https://data.traffic.hereapi.com/v7/flow";
}