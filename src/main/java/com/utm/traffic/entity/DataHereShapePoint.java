package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * HERE 路段形状点子表实体类
 * 对应表: data_here_shape_points
 */
@Data
@TableName("data_here_shape_points")
public class DataHereShapePoint {

    /**
     * 主键 UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;

    /**
     * 关联 data_here_flow 表的主键
     */
    @TableField("flow_uuid")
    private String flowUuid;

    /**
     * 形状点纬度
     */
    private BigDecimal latitude;

    /**
     * 形状点经度
     */
    private BigDecimal longitude;
    /**
     * 道路等级 (1-5)
     */
    @TableField("functional_class")
    private Integer functionalClass;
    /**
     * 总路段名称
     */
    @TableField("all_description_str")
    private String allDescriptionStr;

    /**
     * 总路段长度
     */
    @TableField("all_length")
    private Integer allLength;
    /**
     * 支路长度
     */
    @TableField("zl_length")
    private Integer zlLength;
    /**
     * 形状绘制排序号
     */
    @TableField("sequence_order")
    private Integer sequenceOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}