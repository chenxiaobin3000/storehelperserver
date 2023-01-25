package com.cxb.storehelperserver.util;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
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
    @Resource
    private StorageCommodityRepository storageCommodityRepository;
    @Resource
    private StorageOriginalRepository storageOriginalRepository;
    @Resource
    private StorageHalfgoodRepository storageHalfgoodRepository;
    @Resource
    private StorageStandardRepository storageStandardRepository;
    @Resource
    private StorageDestroyRepository storageDestroyRepository;

    @Resource
    protected RedisTemplate<String, Object> redisTemplate;

    @Value("${store-app.config.cachetime}")
    private int cachetime;

    protected final String cacheCommodity = "sc::";
    protected final String cacheOriginal = "so::";
    protected final String cacheHalfgood = "sh::";
    protected final String cacheStandard = "ss::";
    protected final String cacheDestroy = "sd::";

    /**
     * desc: 读取缓存
     */
    protected Integer getCommodityCache(int sid, int id) {
        Integer ret = (Integer) redisTemplate.opsForValue().get(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(id));
        if (null != ret) {
            return ret;
        }
        TStorageCommodity storageCommodity = storageCommodityRepository.find(sid, id);
        if (null == storageCommodity) {
            return null;
        }
        ret = storageCommodity.getValue();
        setCommodityCache(sid, id, ret);
        return ret;
    }

    protected Integer getOriginalCache(int sid, int id) {
        Integer ret = (Integer) redisTemplate.opsForValue().get(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(id));
        if (null != ret) {
            return ret;
        }
        TStorageOriginal storageOriginal = storageOriginalRepository.find(sid, id);
        if (null == storageOriginal) {
            return null;
        }
        ret = storageOriginal.getValue();
        setOriginalCache(sid, id, ret);
        return ret;
    }

    protected Integer getHalfgoodCache(int sid, int id) {
        Integer ret = (Integer) redisTemplate.opsForValue().get(cacheHalfgood + String.valueOf(sid) + "::" + String.valueOf(id));
        if (null != ret) {
            return ret;
        }
        TStorageHalfgood storageHalfgood = storageHalfgoodRepository.find(sid, id);
        if (null == storageHalfgood) {
            return null;
        }
        ret = storageHalfgood.getValue();
        setHalfgoodCache(sid, id, ret);
        return ret;
    }

    protected Integer getStandardCache(int sid, int id) {
        Integer ret = (Integer) redisTemplate.opsForValue().get(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(id));
        if (null != ret) {
            return ret;
        }
        TStorageStandard storageStandard = storageStandardRepository.find(sid, id);
        if (null == storageStandard) {
            return null;
        }
        ret = storageStandard.getValue();
        setStandardCache(sid, id, ret);
        return ret;
    }

    protected Integer getDestroyCache(int sid, int id) {
        Integer ret = (Integer) redisTemplate.opsForValue().get(cacheDestroy + String.valueOf(sid) + "::" + String.valueOf(id));
        if (null != ret) {
            return ret;
        }
        TStorageDestroy storageDestroy = storageDestroyRepository.find(sid, id);
        if (null == storageDestroy) {
            return null;
        }
        ret = storageDestroy.getValue();
        setDestroyCache(sid, id, ret);
        return ret;
    }

    /**
     * desc: 写入缓存
     */
    protected boolean setCommodityCache(int sid, int cid, int value) {
        TStorageCommodity storageCommodity = storageCommodityRepository.find(sid, cid);
        if (null == storageCommodity) {
            storageCommodity = new TStorageCommodity();
            storageCommodity.setSid(sid);
            storageCommodity.setCid(cid);
            storageCommodity.setValue(value);
            if (!storageCommodityRepository.insert(storageCommodity)) {
                return false;
            }
        } else {
            storageCommodity.setValue(storageCommodity.getValue() + (value));
            if (!storageCommodityRepository.update(storageCommodity)) {
                return false;
            }
        }
        redisTemplate.opsForValue().set(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(cid), value, cachetime, TimeUnit.MINUTES);
        return true;
    }

    protected boolean setOriginalCache(int sid, int cid, int value) {
        TStorageOriginal storageOriginal = storageOriginalRepository.find(sid, cid);
        if (null == storageOriginal) {
            storageOriginal = new TStorageOriginal();
            storageOriginal.setSid(sid);
            storageOriginal.setOid(cid);
            storageOriginal.setValue(value);
            if (!storageOriginalRepository.insert(storageOriginal)) {
                return false;
            }
        } else {
            storageOriginal.setValue(storageOriginal.getValue() + (value));
            if (!storageOriginalRepository.update(storageOriginal)) {
                return false;
            }
        }
        redisTemplate.opsForValue().set(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(cid), value, cachetime, TimeUnit.MINUTES);
        return true;
    }

    protected boolean setHalfgoodCache(int sid, int cid, int value) {
        redisTemplate.opsForValue().set(cacheHalfgood + String.valueOf(sid) + "::" + String.valueOf(cid), value, cachetime, TimeUnit.MINUTES);
        return true;
    }

    protected boolean setStandardCache(int sid, int cid, int value) {
        redisTemplate.opsForValue().set(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(cid), value, cachetime, TimeUnit.MINUTES);
        return true;
    }

    protected boolean setDestroyCache(int sid, int cid, int value) {
        redisTemplate.opsForValue().set(cacheDestroy + String.valueOf(sid) + "::" + String.valueOf(cid), value, cachetime, TimeUnit.MINUTES);
        return true;
    }

    /**
     * desc: 删除缓存
     */
    protected void delCommodityCache(int sid, int id) {
        redisTemplate.delete(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void delOriginalCache(int sid, int id) {
        redisTemplate.delete(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void delHalfgoodCache(int sid, int id) {
        redisTemplate.delete(cacheHalfgood + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void delStandardCache(int sid, int id) {
        redisTemplate.delete(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void delDestroyCache(int sid, int id) {
        redisTemplate.delete(cacheDestroy + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    /**
     * desc: 监控缓存
     */
    protected void watchCommodityCache(int sid, int id) {
        redisTemplate.watch(cacheCommodity + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void watchOriginalCache(int sid, int id) {
        redisTemplate.watch(cacheOriginal + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void watchHalfgoodCache(int sid, int id) {
        redisTemplate.watch(cacheHalfgood + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void watchStandardCache(int sid, int id) {
        redisTemplate.watch(cacheStandard + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    protected void watchDestroyCache(int sid, int id) {
        redisTemplate.watch(cacheDestroy + String.valueOf(sid) + "::" + String.valueOf(id));
    }

    /**
     * desc: 开始事务
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
