package com.utm.traffic.service;

import com.utm.traffic.entity.RedisInternalCombination;

import java.util.List;

/**
 * redis对于key值内部组合输出
 */
public interface IRedisInternalCombinationService {

    //保存方法
    List<RedisInternalCombination> saveRedisInternalCombination(List<RedisInternalCombination> internalCombinations);
}
