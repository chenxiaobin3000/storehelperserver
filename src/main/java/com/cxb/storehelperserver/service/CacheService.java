package com.cxb.storehelperserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * desc: 缓存业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
public class CacheService<Model> {
    @Resource
    protected RedisTemplate<String, Object> redisTemplate;

    @Value("${store-app.config.cachetime}")
    private int cachetime;

    /**
     * desc: 缓存 key 前缀
     */
    protected String cacheName;

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
     * desc: 读取缓存
     */
    protected Model getCache(int id, Class<Model> clazz) {
        return clazz.cast(redisTemplate.opsForValue().get(cacheName + String.valueOf(id)));
    }

    /**
     * desc: 写入缓存
     */
    protected void setCache(String key, Model value) {
        redisTemplate.opsForValue().set(cacheName + key, value, cachetime, TimeUnit.MINUTES);
    }

    /**
     * desc: 写入缓存
     */
    protected void setCache(int id, Model value) {
        redisTemplate.opsForValue().set(cacheName + String.valueOf(id), value, cachetime, TimeUnit.MINUTES);
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

    /**
     * desc: 监控缓存
     */
    protected void watchCache(String key) {
        redisTemplate.watch(cacheName + key);
    }

    /**
     * desc: 监控缓存
     */
    protected void watchCache(int id) {
        redisTemplate.watch(cacheName + String.valueOf(id));
    }

    /**
     * desc: 启动事务
     */
    protected void multiCache() {
        redisTemplate.multi();
    }

    /**
     * desc: 执行事务
     */
    protected List<Object> execCache() {
        return redisTemplate.exec();
    }
}
