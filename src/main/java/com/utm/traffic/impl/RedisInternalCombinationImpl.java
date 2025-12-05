package com.utm.traffic.impl;

import com.hz.utils.UUIDUtils;
import com.utm.traffic.entity.RedisInternalCombination;
import com.utm.traffic.mapper.RedisInternalCombinationMapper;
import com.utm.traffic.service.IRedisInternalCombinationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * redis对于key值内部组合输出
 */
@Slf4j
@Service
public class RedisInternalCombinationImpl implements IRedisInternalCombinationService {
    @Autowired
    private RedisInternalCombinationMapper redisInternalCombinationMapper;

    //批量保存数据
    @Override
//    @Transactional(rollbackFor = Exception.class) // 开启事务
    public List<RedisInternalCombination> saveRedisInternalCombination(List<RedisInternalCombination> redisInternalCombinationList) {
        if (redisInternalCombinationList == null || redisInternalCombinationList.isEmpty()) {
            throw new IllegalArgumentException("RedisInternalCombination 批量保存数失败 生成数据为空！");
        }
        //直接保存数据库
        redisInternalCombinationList.stream().forEach(r->{
            r.setUuid(UUIDUtils.getUUID());
            r.setCreateTime(new Date());
            int insert = redisInternalCombinationMapper.insert(r);
            if (insert != 1) throw new IllegalArgumentException("RedisInternalCombination数控保存失败生成数据为空！");

        });
        log.info("RedisInternalCombination批量保存成功！");
        return redisInternalCombinationList;
    }
}
