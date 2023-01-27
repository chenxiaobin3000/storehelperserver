package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;

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

    public RestResult getStockCommodity(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockCommodityRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockCommodityRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockHalfgood(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockHalfgoodRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockHalfgoodRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取半成品信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockOriginal(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockOriginalRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockOriginalRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取原料信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockStandard(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockStandardRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockStandardRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取标品信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockDestroy(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockDestroyRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockDestroyRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取废料信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult countStock(int id, int sid, Date date) {
        return RestResult.ok();
    }

    public boolean delStock(CommodityType type, int sid, int cid, Date date) {
        switch (type) {
            case COMMODITY:
                return stockCommodityRepository.delete(sid, cid, date);
            case HALFGOOD:
                return stockHalfgoodRepository.delete(sid, cid, date);
            case ORIGINAL:
                return stockOriginalRepository.delete(sid, cid, date);
            case STANDARD:
                return stockStandardRepository.delete(sid, cid, date);
            default:
                return stockDestroyRepository.delete(sid, cid, date);
        }
    }

    private RestResult check(int id, int sid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        TStorage storage = storageRepository.find(sid);
        if (null == storage) {
            return RestResult.fail("获取仓库信息失败");
        }
        if (!group.getGid().equals(storage.getGid())) {
            return RestResult.fail("只能获取本公司信息");
        }
        return null;
    }
}
