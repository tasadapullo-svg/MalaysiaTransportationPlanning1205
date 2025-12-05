package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * redis对于key值内部组合输出
 */
@Data
@TableName("TD.data_redis_internal_combination")//data_redis_internal_combination
public class RedisInternalCombination {

    /**
     * uuid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;

    /**
     * 批量编号
     */
    @TableField("pd_uuid")
    private String pdUuid;

    /**
     * keys
     */
    @TableField("key_number")
    private String keyNumber;

    /**
     * 中心点纬度
     */
    private BigDecimal latitude;

    /**
     * 中心点经度
     */
    private BigDecimal longitude;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
