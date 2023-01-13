package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.StorageCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓库库存业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageStockService extends StorageCache {
    @Resource
    protected RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;

    @Resource
    private StorageOriginalRepository storageOriginalRepository;

    @Resource
    private StorageStandardRepository storageStandardRepository;

    @Resource
    private StorageDestroyRepository storageDestroyRepository;

    public StorageStockService() {
        init(redisTemplate);
    }

    /*
     * desc: 进货
     */
    public RestResult purchase(int id, int sid, int cid, List<Integer> commoditys, List<Integer> values, List<String> prices) {
        // 写入数据库
        // TODO 测试写入失败回滚
        // return fail

        // 写入缓存
        multiCache();

        execCache();

        // 异常就回滚
        // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return RestResult.ok();
    }

    /*
     * desc: 生产开始
     */
    public RestResult process(int id, int gid, int sid, List<Integer> commoditys, List<Integer> values, List<String> prices) {
        return RestResult.ok();
    }

    /*
     * desc: 生产完成
     */
    public RestResult complete(int id, int gid, int sid, List<Integer> commoditys, List<Integer> values, List<String> prices) {
        return RestResult.ok();
    }
}
