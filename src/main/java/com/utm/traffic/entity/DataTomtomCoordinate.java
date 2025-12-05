package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * TomTom 路段坐标子表实体类 (用于存储构成路段的一组经纬度)
 * 对应表: data_tomtom_coordinates
 */
@Data
@TableName("data_tomtom_coordinates")
public class DataTomtomCoordinate {

    /**
     * 主键 UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;

    /**
     * 关联 data_tomtom_flow 表的主键
     */
    private String flowUuid;

    /**
     * 坐标点纬度
     */
    private BigDecimal latitude;

    /**
     * 坐标点经度
     */
    private BigDecimal longitude;

    /**
     * 坐标连线排序号
     */
    private Integer sequenceOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}