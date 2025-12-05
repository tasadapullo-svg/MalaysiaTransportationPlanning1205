package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 监测区域基础信息实体类 (HERE)
 * 对应表: base_monitor_bboxes
 */
@Data
@TableName("base_monitor_bboxes")
public class BaseMonitorBbox {

    /**
     * 主键 UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;

    /**
     * 区域名称 (如 Section A)
     */
    private String name;

    /**
     * bbox
     */
    private String bbox;

    /**
     * API 查询参数串 (如 bbox:...)
     */
    private String queryString;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}