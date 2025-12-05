package com.utm.traffic.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // ------------------- List 操作（API Key 队列）-------------------
    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ------------------- Hash 操作（监测点位缓存）-------------------
    public void hPut(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("RedisUtils set", e);
//            e.printStackTrace();
            return false;
        }
    }

    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // ------------------- 通用操作 -------------------
    //检查值是否存在
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setExpire(String key, long time, TimeUnit unit) {
        redisTemplate.expire(key, time, unit);
    }
    // ------------------- 删除操作 -------------------

    /**
     * 删除单个key
     * @param key 键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 批量删除key
     * @param keys 键的集合
     * @return 删除的数量
     */
    public Long delete(List<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 删除Hash结构中的某个字段
     * @param key 主键
     * @param hashKey 哈希字段
     * @return 是否删除成功
     */
    public boolean hDelete(String key, String hashKey) {
        return redisTemplate.opsForHash().delete(key, hashKey) > 0;
    }

    /**
     * 批量删除Hash结构中的多个字段
     * @param key 主键
     * @param hashKeys 哈希字段集合
     * @return 删除的数量
     */
    public Long hDelete(String key, List<String> hashKeys) {
        // 将List转换为数组（HashOperations.delete接收可变参数）
        Object[] hashKeyArray = hashKeys.toArray();
        return redisTemplate.opsForHash().delete(key, hashKeyArray);
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("RedisUtil expire", e);
            return false;
        }
    }
    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * description: 获取对象类型
     * author yao
     * create time: 2019-12-05 21:11
     *
     * @param key 键
     * @return T 返回类
     */
    public <T> T getDataByType(String key) {
        if (key == null) {
            return null;
        }
        try {
            if (!this.hasKey(key)) return null;
            Object o = redisTemplate.opsForValue().get(key);
            if (o == null) return null;
            return (T) o;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询并返回集合列表
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getToList(String key, Class<T> clazz) {
        try {
            Object o = this.get(key);
            if (o == null) return null;
            List<T> result = new ArrayList<>();
            if (o instanceof ArrayList<?>) {
                for (Object obj : (List<?>) o) {
                    result.add(clazz.cast(obj));
                }
                return result;
            }
            return null;
        } catch (Exception e) {
            log.error("getList", e);
            return null;
        }
    }


}