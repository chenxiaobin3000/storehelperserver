package com.cxb.storehelperserver.util;

import com.cxb.storehelperserver.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * desc: 仓库缓存业务
 * auth: cxb
 * date: 2023/1/13
 */
public class StorageCache {
    @Value("${store-app.config.cachetime}")
    private int cachetime;

    protected final String cacheCommodity = "sc::";
    protected final String cacheOriginal = "so::";
    protected final String cacheHalfgood = "sh::";
    protected final String cacheStandard = "ss::";
    protected final String cacheDestroy = "sd::";

    protected RedisTemplate<String, Object> redis;

    /**
     * desc: 初始化缓存
     */
    protected void init(RedisTemplate<String, Object> redisTemplate) {
        this.redis = redisTemplate;
    }

    /**
     * desc: 读取缓存
     */
    protected TStorageCommodity getCommodityCache(int sid, int id) {
        return (TStorageCommodity) redis.opsForValue().get(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected TStorageOriginal getOriginalCache(int sid, int id) {
        return (TStorageOriginal) redis.opsForValue().get(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected TStorageHalfgood getHalfgoodCache(int sid, int id) {
        return (TStorageHalfgood) redis.opsForValue().get(cacheHalfgood + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected TStorageStandard getStandardCache(int sid, int id) {
        return (TStorageStandard) redis.opsForValue().get(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected TStorageDestroy getDestroyCache(int sid, int id) {
        return (TStorageDestroy) redis.opsForValue().get(cacheDestroy + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    /**
     * desc: 写入缓存
     */
    protected void setCommodityCache(int sid, int id, TStorageCommodity value) {
        redis.opsForValue().set(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(id), value, cachetime, TimeUnit.MINUTES);
    }

    protected void setOriginalCache(int sid, int id, TStorageOriginal value) {
        redis.opsForValue().set(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(id), value, cachetime, TimeUnit.MINUTES);
    }

    protected void setHalfgoodCache(int sid, int id, TStorageHalfgood value) {
        redis.opsForValue().set(cacheHalfgood + String.valueOf(sid) + "::" + String.valueOf(id), value, cachetime, TimeUnit.MINUTES);
    }

    protected void setStandardCache(int sid, int id, TStorageStandard value) {
        redis.opsForValue().set(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(id), value, cachetime, TimeUnit.MINUTES);
    }

    protected void setDestroyCache(int sid, int id, TStorageDestroy value) {
        redis.opsForValue().set(cacheDestroy + String.valueOf(sid) + "::" + String.valueOf(id), value, cachetime, TimeUnit.MINUTES);
    }

    /**
     * desc: 删除缓存
     */
    protected void delCommodityCache(int sid, int id) {
        redis.delete(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void delOriginalCache(int sid, int id) {
        redis.delete(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void delHalfgoodCache(int sid, int id) {
        redis.delete(cacheHalfgood + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void delStandardCache(int sid, int id) {
        redis.delete(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void delDestroyCache(int sid, int id) {
        redis.delete(cacheDestroy + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    /**
     * desc: 监控缓存
     */
    protected void watchCommodityCache(int sid, int id) {
        redis.watch(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void watchOriginalCache(int sid, int id) {
        redis.watch(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void watchHalfgoodCache(int sid, int id) {
        redis.watch(cacheHalfgood + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void watchStandardCache(int sid, int id) {
        redis.watch(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void watchDestroyCache(int sid, int id) {
        redis.watch(cacheDestroy + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    /**
     * desc: 开始事务
     */
    protected void multiCache() {
        redis.multi();
    }

    /**
     * desc: 执行事务
     */
    protected List<Object> execCache() {
        return redis.exec();
    }
}
