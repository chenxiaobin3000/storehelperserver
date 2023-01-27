package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * desc: 库存统计业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StockService {
    @Resource
    private StockCommodityRepository stockCommodityRepository;

    @Resource
    private StockHalfgoodRepository stockHalfgoodRepository;

    @Resource
    private StockOriginalRepository stockOriginalRepository;

    @Resource
    private StockStandardRepository stockStandardRepository;

    @Resource
    private StockDestroyRepository stockDestroyRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private StorageRepository storageRepository;

    public RestResult getStockCommodity(int id, int sid, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        TStorage storage = storageRepository.find(sid);
        if (null == storage) {
            return RestResult.fail("获取仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getStockHalfgood(int id, int sid, int page, int limit, String search) {
        return RestResult.ok();
    }

    public RestResult getStockOriginal(int id, int sid, int page, int limit, String search) {
        return RestResult.ok();
    }

    public RestResult getStockStandard(int id, int sid, int page, int limit, String search) {
        return RestResult.ok();
    }

    public RestResult getStockDestroy(int id, int sid, int page, int limit, String search) {
        return RestResult.ok();
    }

    public RestResult countStock(int id, int sid, String date) {
        return RestResult.ok();
    }
}
