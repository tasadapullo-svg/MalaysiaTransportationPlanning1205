package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 任务执行日志实体类
 * 对应表: sys_job_logs
 */
@Data
@TableName("sys_job_logs")
public class SysJobLog {

    /**
     * 主键 UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;

    /**
     * 任务名称 (如 TOMTOM_POLLING_JOB)
     */
    private String jobName;

    /**
     * 任务开始时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 任务结束时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 执行状态 (SUCCESS, FAILED, RUNNING)
     */
    private String status;

    /**
     * 计划采集点位数
     */
    private Integer totalPoints;

    /**
     * 成功采集点位数
     */
    private Integer successPoints;

    /**
     * 异常堆栈信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}