package com.cxb.storehelperserver.repository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * desc: 基础仓库，提供缓存支持
 * auth: cxb
 * date: 2022/11/30
 */
@Slf4j
public class BaseRepository<Model> {
    @Resource
    protected RedisTemplate<String, Object> redisTemplate;

    /**
     * desc: 缓存 key 前缀
     */
    protected String cacheName;

    /**
     * desc: 缓存总量
     */
    protected String cacheTotal;

    /**
     * desc: 初始化缓存 key 前缀
     */
    protected void init(String cacheName) {
        this.cacheName = cacheName;
        this.cacheTotal = this.cacheName + "total";
    }

    /**
     * desc: 读取缓存
     */
    protected Model getCache(String key, Class<Model> clazz) {
        return clazz.cast(redisTemplate.opsForValue().get(cacheName + key));
    }

    /**
     * desc: 读取缓存
     */
    protected Model getCache(int id, Class<Model> clazz) {
        return clazz.cast(redisTemplate.opsForValue().get(cacheName + String.valueOf(id)));
    }

    /**
     * desc: 写入缓存
     */
    protected void setCache(String key, Model value) {
        redisTemplate.opsForValue().set(cacheName + key, value);
    }

    /**
     * desc: 写入缓存
     */
    protected void setCache(int id, Model value) {
        redisTemplate.opsForValue().set(cacheName + String.valueOf(id), value);
    }

    /**
     * desc: 写入缓存，可设置超时，单位：分钟
     */
    protected void setCacheExpire(String key, Model value, long timeout) {
        redisTemplate.opsForValue().set(cacheName + key, value, timeout, TimeUnit.MINUTES);
    }

    /**
     * desc: 写入缓存，可设置超时，单位：分钟
     */
    protected void setCacheExpire(int id, Model value, long timeout) {
        redisTemplate.opsForValue().set(cacheName + String.valueOf(id), value, timeout, TimeUnit.MINUTES);
    }

    /**
     * desc: 删除缓存
     */
    protected void delCache(String key) {
        redisTemplate.delete(cacheName + key);
    }

    /**
     * desc: 删除缓存
     */
    protected void delCache(int id) {
        redisTemplate.delete(cacheName + String.valueOf(id));
    }

    // --------------- 总数缓存 ---------------
    /**
     * desc: 读取总数缓存
     */
    protected int getTotalCache(int id) {
        val ret = redisTemplate.opsForValue().get(cacheTotal + String.valueOf(id));
        if (null == ret) {
            return 0;
        }
        return (int) ret;
    }

    /**
     * desc: 写入总数缓存
     */
    protected void setTotalCache(int id, int value) {
        redisTemplate.opsForValue().set(cacheTotal + String.valueOf(id), value);
    }

    /**
     * desc: 写入总数缓存，可设置超时，单位：分钟
     */
    protected void setTotalCacheExpire(int id, int value, long timeout) {
        redisTemplate.opsForValue().set(cacheTotal + String.valueOf(id), value, timeout, TimeUnit.MINUTES);
    }

    /**
     * desc: 删除总数缓存
     */
    protected void delTotalCache(int id) {
        redisTemplate.delete(cacheTotal + String.valueOf(id));
    }
}
