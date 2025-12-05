package com.utm.traffic.scheduler;

import com.utm.traffic.entity.LogKeyUsage;
import com.utm.traffic.entity.SysApiKey;
import com.utm.traffic.mapper.LogKeyUsageMapper;
import com.utm.traffic.mapper.SysApiKeyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 每天 00:00:30 初始化 log_key_usage（避免 UPDATE 无行影响）
 */
@Slf4j
@Component
public class LogKeyInitScheduler {

    @Autowired
    private SysApiKeyMapper sysApiKeyMapper;

    @Autowired
    private LogKeyUsageMapper logKeyUsageMapper;

    @Scheduled(cron = "30 0 0 * * ?")
    public void initDailyKeyUsage() {
        try {
            List<SysApiKey> keys = sysApiKeyMapper.selectList(null);
            Date today = new Date(); // usage_date will store date (time portion included; DB compare should match your DB's date format or truncate as needed)
            for (SysApiKey key : keys) {
                // Try insert; if unique constraint on (api_key_uuid, usage_date) exists DB will reject duplicate; better to check then insert.
                LogKeyUsage r = new LogKeyUsage();
                r.setUuid(UUID.randomUUID().toString());
                r.setApiKeyUuid(key.getUuid());
                r.setUsageDate(today);
                r.setRequestCount(0);
                r.setErrorCount(0);
                r.setUpdatedAt(today);
                r.setCreateTime(new Date());
                try {
                    logKeyUsageMapper.insertOne(r);
                } catch (Exception ex) {
                    // already exists or other error -> ignore for duplication
                    log.debug("初始化 log_key_usage（{}）已存在或失败：{}", key.getUuid(), ex.getMessage());
                }
            }
            log.info("每日 log_key_usage 初始化已完成，keys={}", keys.size());
        } catch (Exception e) {
            log.error("每日初始化 log_key_usage 失败", e);
        }
    }
}
