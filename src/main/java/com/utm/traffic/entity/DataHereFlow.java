package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * HERE 区域路况主表实体类
 * 对应表: data_here_flow
 */
@Data
@TableName("data_here_flow")
public class DataHereFlow {

    /**
     * 主键 UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;

    /**
     * 关联 base_monitor_bboxes 表的主键
     */
    private String bboxUuid;

    /**
     * 区域编号
     */
    private String batchId;

    /**
     * 区域编号
     */
    private String placeId;

    /**
     * 数据采集时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date captureTime;

    /**
     * 道路名称
     */
    private String roadName;

    /**
     * 包含多少道路
     */
    private Integer numberRoads;

    /**
     * 实时速度 (已转换为 km/h)
     */
    private BigDecimal speedKmh;

    /**
     * 拥堵指数 (0.0 - 10.0)
     */
    private BigDecimal jamFactor;

    /**
     * 创建时间 (入库时间)
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}