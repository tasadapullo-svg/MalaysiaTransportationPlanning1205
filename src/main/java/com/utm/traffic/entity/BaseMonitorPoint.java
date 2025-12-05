package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 监测点位基础信息实体类 (TomTom)
 * 对应表: base_monitor_points
 */
@Data
@TableName("base_monitor_points")
public class BaseMonitorPoint {

    /**
     * 主键 UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;

    /**
     * 原始 CSV 中的编号 (如 P1A)
     */
    private String originalId;

    /**
     * 中心点纬度
     */
    private BigDecimal latitude;

    /**
     * 中心点经度
     */
    private BigDecimal longitude;

    /**
     * 道路上的排序号 (用于连线)
     */
    private Integer sequenceOrder;

    /**
     * 分配给哪个 Key 策略组
     */
    private Integer assignedKeyGroup;

    /**
     * 是否启用该点位
     */
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
