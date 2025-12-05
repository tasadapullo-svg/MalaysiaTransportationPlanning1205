package com.utm.traffic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.utm.traffic.entity.LogKeyUsage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;

import java.util.Date;

@Mapper
public interface LogKeyUsageMapper extends BaseMapper<LogKeyUsage> {

    @Update("UPDATE log_key_usage " +
            "SET request_count = request_count + 1, updated_at = #{time} " +
            "WHERE api_key_uuid = #{uuid} AND usage_date = #{date}")
    int addRequestCount(@Param("uuid") String uuid,
                        @Param("date") Date date,
                        @Param("time") Date time);

    @Update("UPDATE log_key_usage " +
            "SET error_count = error_count + 1, updated_at = #{time} " +
            "WHERE api_key_uuid = #{uuid} AND usage_date = #{date}")
    int addErrorCount(@Param("uuid") String uuid,
                      @Param("date") Date date,
                      @Param("time") Date time);

    @Insert("INSERT INTO log_key_usage(uuid, api_key_uuid, usage_date, request_count, error_count, updated_at, create_time) " +
            "VALUES(#{uuid}, #{apiKeyUuid}, #{usageDate}, #{requestCount}, #{errorCount}, #{updatedAt}, #{createTime})")
    int insertOne(LogKeyUsage record);
}
