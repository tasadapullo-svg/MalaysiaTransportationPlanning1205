package com.utm.traffic.utils;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.utm.traffic.constant.RedisKeyConstant;
import com.utm.traffic.entity.SysUserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class SysUserAgentRedisUtil {
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 从 Redis 加载 User-Agent，并随机返回一个 uaText
     */
    public String getSysUserAgentRedis() {

        String json = redisUtil.getDataByType(RedisKeyConstant.USER_AHENt_KEY);
        if (StringUtils.isEmpty(json)) {
            throw new RuntimeException("Redis 未找到 User-Agent 数据，key=" + RedisKeyConstant.USER_AHENt_KEY);
        }

        List<SysUserAgent> list = JSONArray.parseArray(json, SysUserAgent.class);
        if (list == null || list.isEmpty()) {
            throw new RuntimeException("Redis 中 User-Agent 列表为空，无法随机选择");
        }

        SysUserAgent randomUa = list.get(new Random().nextInt(list.size()));

        if (randomUa == null || StringUtils.isEmpty(randomUa.getUaText())) {
            throw new RuntimeException("User-Agent 数据异常：uaText 为空");
        }

        return randomUa.getUaText();
    }

}
