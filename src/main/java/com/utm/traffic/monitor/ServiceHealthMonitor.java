package com.utm.traffic.monitor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.utm.traffic.constant.RedisKeyConstant;
import com.utm.traffic.entity.SysJobLog;
import com.utm.traffic.mapper.SysJobLogMapper;
import com.utm.traffic.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class ServiceHealthMonitor {

    @Autowired private RedisUtil redisUtil;
    @Autowired private JavaMailSender mailSender;
    @Autowired private SysJobLogMapper sysJobLogMapper;

    // 修改为 15 分钟
    private static final long FIFTEEN_MINUTE = 15 * 60 * 1000L;
    private boolean emailSent = false;

    @Scheduled(fixedDelay = 30000)
    public void checkServiceStatus() {
        try {
            Object alw = redisUtil.get(RedisKeyConstant.SERVICE_LAST_ALIVE);

            // ★★★★★ 自愈初始化
            if (alw == null) {
                long now = System.currentTimeMillis();
                redisUtil.set(RedisKeyConstant.SERVICE_LAST_ALIVE, now);
                log.warn("首次检测无心跳 → 已自动初始化 SERVICE_LAST_ALIVE={}", now);
                return;
            }

            long last = Long.parseLong(alw.toString());
            long diff = System.currentTimeMillis() - last;

            // ★ 超过15分钟无心跳 → 告警
            if (diff > FIFTEEN_MINUTE && !emailSent) {
                SysJobLog logSysJob = getLatestFailedJob();
                String details = formatJobLog(logSysJob);
                boolean ok = sendEmail("【严重告警】服务超过 15 分钟无心跳！\n" + details);

                if (ok) {
                    emailSent = true;
                    log.error("服务超过15分钟无心跳 → 告警邮件已发送");
                }
                return;
            }

            // ★ 服务恢复：1分钟内有心跳 → 发恢复通知
            if (diff < 60 * 1000L && emailSent) {
                boolean ok = sendRecoveryEmail("【恢复通知】服务已恢复正常，最近心跳：" + new Date(last));
                if (ok) {
                    log.info("服务已恢复 → 恢复邮件已发送");
                }
                emailSent = false;
            }

        } catch (Exception e) {
            log.error("心跳监控失败", e);
        }
    }

    // -------- 获取最近失败任务 --------

    private SysJobLog getLatestFailedJob() {
        return sysJobLogMapper.selectOne(
                new LambdaQueryWrapper<SysJobLog>()
                        .eq(SysJobLog::getStatus, "FAIL")
                        .orderByDesc(SysJobLog::getEndTime)
                        .last("LIMIT 1")
        );
    }

    private String formatJobLog(SysJobLog log) {
        if (log == null) {
            return "\n【最近失败任务】无失败记录\n";
        }

        return "\n【最近失败任务记录】\n"
                + "任务名称：" + log.getJobName() + "\n"
                + "开始时间：" + log.getStartTime() + "\n"
                + "结束时间：" + log.getEndTime() + "\n"
                + "状态：" + log.getStatus() + "\n"
                + "错误信息：" + log.getErrorMessage() + "\n"
                + "总点位数：" + log.getTotalPoints() + "\n"
                + "成功点位数：" + log.getSuccessPoints() + "\n";
    }

    // -------- 邮件发送 --------

    private boolean sendEmail(String content) {
        return send("【严重告警】服务异常 - ", content);
    }

    private boolean sendRecoveryEmail(String content) {
        return send("【恢复通知】服务已恢复 - ", content);
    }

    private boolean send(String titlePrefix, String content) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("sunwenjie202410@163.com");
            msg.setTo("sunwenjie202410@163.com");
            msg.setSubject(titlePrefix + new Date());
            msg.setText(content);
            mailSender.send(msg);
            return true;
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            return false;
        }
    }
}
