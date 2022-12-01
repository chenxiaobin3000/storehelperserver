package com.cxb.storehelperserver.repository;

import lombok.extern.slf4j.Slf4j;
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
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * desc: 缓存 key 前缀
     */
    private String cacheName;

    /**
     * desc: 初始化缓存 key 前缀
     */
    protected void init(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * desc: 读取缓存
     */
    protected Model getCache(String key, Class<Model> clazz) {
        return clazz.cast(redisTemplate.opsForValue().get(cacheName + key));
    }

    /**
     * desc: 写入缓存
     */
    protected void setCache(String key, Model value) {
        redisTemplate.opsForValue().set(cacheName + key, value);
    }

    /**
     * desc: 写入缓存，可设置超时，单位：分钟
     */
    protected void setCacheExpire(String key, Model value, long timeout) {
        redisTemplate.opsForValue().set(cacheName + key, value, timeout, TimeUnit.MINUTES);
    }
}
