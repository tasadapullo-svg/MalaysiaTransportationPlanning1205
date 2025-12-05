package com.utm.traffic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("sys_user_agent")
public class SysUserAgent {
    /**
     * 主键 UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;


    /**
     * 请求头描述
     */
    private String uaText;

    /**
     * 正常启用是1
     */
    private Integer statusKey;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
