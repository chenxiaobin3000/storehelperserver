package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
import com.cxb.storehelperserver.repository.model.MyMarketSaleInfo;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.*;

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
    private MarketCommodityDetailRepository marketCommodityDetailRepository;

    @Resource
    private MarketStandardRepository marketStandardRepository;

    @Resource
    private MarketStandardDetailRepository marketStandardDetailRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private CommodityAttrRepository commodityAttrRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private StandardAttrRepository standardAttrRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

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

    public RestResult getMarketCommodity(int id, int page, int limit, int mid, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int total = commodityRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val list = marketCommodityRepository.pagination(group.getGid(), page, limit, mid, search);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }

        // 属性
        for (MyMarketCommodity c : list) {
            val attrs = commodityAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val tmp = new ArrayList<String>();
                c.setAttrs(tmp);
                for (TCommodityAttr attr : attrs) {
                    tmp.add(attr.getValue());
                }
            }
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list);
        return RestResult.ok(data);
    }

    public RestResult setMarketStandard(int id, int gid, int mid, int cid, String name, BigDecimal price) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        TMarketStandard standard = new TMarketStandard();
        standard.setGid(gid);
        standard.setMid(mid);
        standard.setCid(cid);
        standard.setName(name);
        standard.setPrice(price);
        if (!marketStandardRepository.update(standard)) {
            return RestResult.fail("修改对接标品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delMarketStandard(int id, int gid, int mid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!marketStandardRepository.delete(gid, mid, cid)) {
            return RestResult.fail("删除对接标品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketStandard(int id, int page, int limit, int mid, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int total = standardRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val list = marketStandardRepository.pagination(group.getGid(), page, limit, mid, search);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }

        // 属性
        for (MyMarketCommodity c : list) {
            val attrs = standardAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val tmp = new ArrayList<String>();
                c.setAttrs(tmp);
                for (TStandardAttr attr : attrs) {
                    tmp.add(attr.getValue());
                }
            }
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list);
        return RestResult.ok(data);
    }

    public RestResult setMarketCommDetail(int id, int gid, TMarketCommodityDetail detail) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (null == detail.getId()) {
            if (!marketCommodityDetailRepository.insert(detail)) {
                return RestResult.fail("插入商品销售信息失败");
            }
        } else {
            // 只处理当天信息
            Date today = dateUtil.getEndTime(dateUtil.addOneDay(new Date(), -1));
            if (detail.getCdate().before(today)) {
                return RestResult.fail("只能更新当日销售信息");
            }
            if (!marketCommodityDetailRepository.update(detail)) {
                return RestResult.fail("更新信息失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delMarketCommDetail(int id, int gid, int did) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        TMarketCommodityDetail detail = marketCommodityDetailRepository.find(did);
        if (null == detail) {
            return RestResult.fail("未查询到商品销售信息");
        }
        // 只处理当天信息
        Date today = dateUtil.getEndTime(dateUtil.addOneDay(new Date(), -1));
        if (detail.getCdate().before(today)) {
            return RestResult.fail("只能更新当日信息");
        }
        if (!marketCommodityDetailRepository.delete(did)) {
            return RestResult.fail("删除商品销售信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketCommDetail(int id, int page, int limit, int mid, Date date, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int total = marketCommodityRepository.total(group.getGid(), mid, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val list = marketCommodityRepository.paginationDetail(group.getGid(), page, limit, mid, date, search);
        if (null == list) {
            return RestResult.fail("未查询到商品销售信息");
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list);
        return RestResult.ok(data);
    }

    public RestResult setMarketStanDetail(int id, int gid, TMarketStandardDetail detail) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (null == detail.getId()) {
            if (!marketStandardDetailRepository.insert(detail)) {
                return RestResult.fail("插入商品销售信息失败");
            }
        } else {
            // 只处理当天信息
            Date today = dateUtil.getEndTime(dateUtil.addOneDay(new Date(), -1));
            if (detail.getCdate().before(today)) {
                return RestResult.fail("只能更新当日销售信息");
            }
            if (!marketStandardDetailRepository.update(detail)) {
                return RestResult.fail("更新信息失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delMarketStanDetail(int id, int gid, int did) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        TMarketStandardDetail detail = marketStandardDetailRepository.find(did);
        if (null == detail) {
            return RestResult.fail("未查询到商品销售信息");
        }
        // 只处理当天信息
        Date today = dateUtil.getEndTime(dateUtil.addOneDay(new Date(), -1));
        if (detail.getCdate().before(today)) {
            return RestResult.fail("只能更新当日信息");
        }
        if (!marketStandardDetailRepository.delete(did)) {
            return RestResult.fail("删除商品销售信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketStanDetail(int id, int page, int limit, int mid, Date date, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int total = marketStandardRepository.total(group.getGid(), mid, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val list = marketStandardRepository.paginationDetail(group.getGid(), page, limit, mid, date, search);
        if (null == list) {
            return RestResult.fail("未查询到标品销售信息");
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list);
        return RestResult.ok(data);
    }

    public RestResult getCommoditySaleInfo(int id, int page, int limit, int mid, ReportCycleType cycle, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int gid = group.getGid();
        int total = commodityRepository.total(gid, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = commodityRepository.pagination(gid, page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }

        // 销售数据
        val cids = new ArrayList<Integer>();
        for (TCommodity c : commodities) {
            cids.add(c.getId());
        }
        val list = marketCommodityDetailRepository.findInCids(group.getGid(), mid, cids);

        SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
        val datas = new ArrayList<HashMap<String, Object>>();
        for (TCommodity c : commodities) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("price", c.getPrice().floatValue());
            tmp.put("unit", c.getUnit());
            tmp.put("remark", c.getRemark());
            datas.add(tmp);

            // 属性
            List<TCommodityAttr> attrs = commodityAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list1 = new ArrayList<String>();
                tmp.put("attrs", list1);
                for (TCommodityAttr attr : attrs) {
                    list1.add(attr.getValue());
                }
            }

            // 销售数据
            val details = new HashMap<String, HashMap<String, Object>>();
            int value = 0;
            BigDecimal price2 = new BigDecimal(0);
            for (MyMarketSaleInfo detail : list) {
                if (detail.getCid().equals(c.getId())) {
                    value += detail.getValue();
                    price2 = price2.add(detail.getTotal());

                    val data = new HashMap<String, Object>();
                    data.put("value", detail.getValue());
                    data.put("total", detail.getTotal());
                    details.put(simpleDateFormat.format(detail.getCdate()), data);
                }
            }
            tmp.put("detail", details);
            tmp.put("value", value);
            tmp.put("total", price2);
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", datas);
        return RestResult.ok(data);
    }

    public RestResult getStandardSaleInfo(int id, int page, int limit, int mid, ReportCycleType cycle, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int gid = group.getGid();
        int total = standardRepository.total(gid, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val standards = standardRepository.pagination(gid, page, limit, search);
        if (null == standards) {
            return RestResult.fail("获取标品信息失败");
        }

        // 销售数据
        val cids = new ArrayList<Integer>();
        for (TStandard c : standards) {
            cids.add(c.getId());
        }
        val list = marketStandardDetailRepository.findInCids(group.getGid(), mid, cids);

        SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
        val datas = new ArrayList<HashMap<String, Object>>();
        for (TStandard c : standards) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("price", c.getPrice().floatValue());
            tmp.put("unit", c.getUnit());
            tmp.put("remark", c.getRemark());
            datas.add(tmp);

            // 属性
            List<TStandardAttr> attrs = standardAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list1 = new ArrayList<String>();
                tmp.put("attrs", list1);
                for (TStandardAttr attr : attrs) {
                    list1.add(attr.getValue());
                }
            }

            // 销售数据
            val details = new HashMap<String, HashMap<String, Object>>();
            int value = 0;
            BigDecimal price2 = new BigDecimal(0);
            for (MyMarketSaleInfo detail : list) {
                if (detail.getCid().equals(c.getId())) {
                    value += detail.getValue();
                    price2 = price2.add(detail.getTotal());

                    val data = new HashMap<String, Object>();
                    data.put("value", detail.getValue());
                    data.put("total", detail.getTotal());
                    details.put(simpleDateFormat.format(detail.getCdate()), data);
                }
            }
            tmp.put("detail", details);
            tmp.put("value", value);
            tmp.put("total", price2);
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", datas);
        return RestResult.ok(data);
    }
}
