package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * API 密钥管理实体类
 * 对应表: sys_api_keys
 */
@Data
@TableName("sys_api_keys")
public class SysApiKey {

    /**
     * 主键 UUID (由 MyBatis Plus 自动生成)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;

    /**
     * 平台类型 (TOMTOM, HERE)
     */
    private String platform;

    /**
     * API Key 字符串
     */
    private String apiKey;

    /**
     * 绑定邮箱 (用于找回或备注)
     */
    private String accountEmail;

    /**
     * 每日调用限额
     */
    private Integer dailyLimit;

    /**
     * 每月调用限额
     */
    private Integer monthlyLimit;

    /**
     * 策略分组ID (用于将Key分配给不同的采集任务)
     */
    private Integer strategyGroup;

    /**
     * 是否启用 (true:启用, false:禁用)
     */
    private Boolean isActive;

    /**
     * 状态 (ACTIVE:活跃, BANNED:被封, RESERVE:备用)
     */
    private String status;

    /**
     * 创建时间 (插入时自动填充)
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}