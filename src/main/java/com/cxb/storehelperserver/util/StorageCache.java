package com.cxb.storehelperserver.util;

import com.cxb.storehelperserver.model.TCommodity;
import com.cxb.storehelperserver.model.TDestroy;
import com.cxb.storehelperserver.model.TOriginal;
import com.cxb.storehelperserver.model.TStandard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
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
    protected TCommodity getCommodityCache(int sid, int id) {
        return (TCommodity) redis.opsForValue().get(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected TOriginal getOriginalCache(int sid, int id) {
        return (TOriginal) redis.opsForValue().get(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected TStandard getStandardCache(int sid, int id) {
        return (TStandard) redis.opsForValue().get(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected TDestroy getDestroyCache(int sid, int id) {
        return (TDestroy) redis.opsForValue().get(cacheDestroy + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    /**
     * desc: 写入缓存
     */
    protected void setCommodityCache(int sid, int id, TCommodity value) {
        redis.opsForValue().set(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(id), value, cachetime, TimeUnit.MINUTES);
    }

    protected void setOriginalCache(int sid, int id, TOriginal value) {
        redis.opsForValue().set(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(id), value, cachetime, TimeUnit.MINUTES);
    }

    protected void setStandardCache(int sid, int id, TStandard value) {
        redis.opsForValue().set(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(id), value, cachetime, TimeUnit.MINUTES);
    }

    protected void setDestroyCache(int sid, int id, TDestroy value) {
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
