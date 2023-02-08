package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TMarketCommodity;
import com.cxb.storehelperserver.model.TMarketDetail;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.MarketCommodityRepository;
import com.cxb.storehelperserver.repository.MarketDetailRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * desc: 市场业务
 * auth: cxb
 * date: 2023/2/7
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class MarketService {
    @Resource
    private CheckService checkService;

    @Resource
    private MarketCommodityRepository marketCommodityRepository;

    @Resource
    private MarketDetailRepository marketDetailRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult setMarketCommodity(int id, int gid, int mid, int cid, String name, BigDecimal price) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        TMarketCommodity commodity = new TMarketCommodity();
        commodity.setGid(gid);
        commodity.setMid(mid);
        commodity.setCid(cid);
        commodity.setName(name);
        commodity.setPrice(price);
        if (!marketCommodityRepository.update(commodity)) {
            return RestResult.fail("修改对接商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delMarketCommodity(int id, int gid, int mid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!marketCommodityRepository.delete(gid, mid, cid)) {
            return RestResult.fail("删除对接商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult addMarketDetail(int id, int gid, int mid, int cid, int value, BigDecimal price) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        TMarketDetail detail = new TMarketDetail();
        detail.setGid(gid);
        detail.setMid(mid);
        detail.setCid(cid);
        detail.setValue(value);
        detail.setPrice(price);
        if (!marketDetailRepository.insert(detail)) {
            return RestResult.fail("添加商品销售信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setMarketDetail(int id, int gid, int did, int value, BigDecimal price) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        TMarketDetail detail = new TMarketDetail();
        detail.setId(did);
        detail.setValue(value);
        detail.setPrice(price);
        if (!marketDetailRepository.insert(detail)) {
            return RestResult.fail("添加商品销售信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delMarketDetail(int id, int gid, int did) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!marketDetailRepository.delete(did)) {
            return RestResult.fail("删除商品销售信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketDetail(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int total = marketDetailRepository.total(group.getGid());
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val list = marketDetailRepository.pagination(group.getGid(), page, limit);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list);
        return RestResult.ok(data);
    }
}
