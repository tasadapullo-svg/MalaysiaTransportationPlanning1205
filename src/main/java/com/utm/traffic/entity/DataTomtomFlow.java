package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * TomTom 实时流数据主表实体类
 * 对应表: data_tomtom_flow
 */
@Data
@TableName("data_tomtom_flow")
public class DataTomtomFlow {

    /**
     * 主键 UUID
     */
    @TableId(type = IdType.AUTO)
    private String uuid;

    /**
     * 关联 base_monitor_points 表的主键
     */
    private String pointUuid;

    /**
     * 数据采集时间 (核心索引字段)
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date captureTime;

    /**
     * 实时速度 (km/h)
     */

    @TableField("current_speed")
    private Integer currentSpeed;

    /**
     * 自由流速度 (km/h)
     */

    @TableField("free_flow_speed")
    private Integer freeFlowSpeed;


    /**
     * 旅行时间
     */

    @TableField("current_travel_time")
    private Integer currentTravelTime;


    /**
     * 自由时间
     */
    @TableField("free_flow_travel_time")
    private Integer freeFlowTravelTime;

    /**
     * 实时通行时间 (秒)
     */
    private Integer travelTime;

    /**
     * 数据置信度 (0.00 - 1.00)
     */
    private BigDecimal confidence;

    /**
     * 是否封路
     */

    @TableField("road_closure")
    private Boolean roadClosure;

    /**
     * 创建时间 (入库时间)
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


}