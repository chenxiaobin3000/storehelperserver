package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyMarketStorage;
import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
import com.cxb.storehelperserver.repository.model.MyMarketSaleInfo;
import com.cxb.storehelperserver.service.model.PageData;
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
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.COMMODITY;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.STANDARD;

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
    private MarketStorageRepository marketStorageRepository;

    @Resource
    private MarketAccountRepository marketAccountRepository;

    @Resource
    private MarketManyRepository marketManyRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private CommodityAttrRepository commodityAttrRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private StandardAttrRepository standardAttrRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult setMarketCommodity(int id, int gid, int sid, int aid, int asid, int cid, String code, String name, String remark, BigDecimal price) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        int mid = 0;
        if (0 == asid) {
            TMarketAccount account = marketAccountRepository.find(aid);
            if (null == account) {
                return RestResult.fail("未查询到账号信息");
            }
            mid = account.getMid();
        } else {
            TMarketMany many = marketManyRepository.find(asid);
            if (null == many) {
                return RestResult.fail("未查询到子账号信息");
            }
            mid = many.getMid();
        }
        TMarketCommodity commodity = new TMarketCommodity();
        commodity.setGid(gid);
        commodity.setSid(sid);
        commodity.setMid(mid);
        commodity.setAid(aid);
        commodity.setAsid(asid);
        commodity.setCid(cid);
        commodity.setCode(code);
        commodity.setName(name);
        commodity.setRemark(remark);
        commodity.setPrice(price);
        if (!marketCommodityRepository.update(commodity)) {
            return RestResult.fail("修改对接商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delMarketCommodity(int id, int gid, int sid, int aid, int asid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!marketCommodityRepository.delete(sid, aid, asid, cid)) {
            return RestResult.fail("删除对接商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketCommodity(int id, int gid, int page, int limit, int sid, int aid, int asid, String search) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        int total = marketCommodityRepository.total(sid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketCommodityRepository.pagination(sid, aid, asid, page, limit, search);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }

        // 属性
        val datas = new ArrayList<HashMap<String, Object>>();
        for (TMarketCommodity c : list) {
            int cid = c.getCid();
            Integer sid2 = c.getSid();
            Integer aid2 = c.getAid();
            Integer asid2 = c.getAsid();
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("cid", cid);
            tmp.put("sid", sid2);
            tmp.put("mid", c.getMid());
            tmp.put("aid", aid2);
            tmp.put("asid", asid2);
            tmp.put("mcode", c.getCode());
            tmp.put("mname", c.getName());
            tmp.put("mremark", c.getRemark());
            tmp.put("alarm", c.getPrice());
            datas.add(tmp);

            if (null != sid2) {
                // 仓库
                TStorage storage = storageRepository.find(sid2);
                if (null != storage) {
                    tmp.put("storage", storage.getName());
                }
            }

            // 平台账号
            if (null != aid2) {
                TMarketAccount account = marketAccountRepository.find(aid2);
                if (null != account) {
                    tmp.put("account", account.getAccount());
                }
            }

            // 商品信息
            val commodity = commodityRepository.find(cid);
            if (null != commodity) {
                tmp.put("category", commodity.getCid());
                tmp.put("ccode", commodity.getCode());
                tmp.put("cname", commodity.getName());
                tmp.put("cremark", commodity.getRemark());
            }

            // 商品属性
            val attrs = commodityAttrRepository.find(cid);
            if (null != attrs && !attrs.isEmpty()) {
                val tmp2 = new ArrayList<String>();
                tmp.put("attrs", tmp2);
                for (TCommodityAttr attr : attrs) {
                    tmp2.add(attr.getValue());
                }
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult setMarketStandard(int id, int gid, int sid, int aid, int asid, int cid, String code, String name, String remark, BigDecimal price) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        int mid = 0;
        if (0 == asid) {
            TMarketAccount account = marketAccountRepository.find(aid);
            if (null == account) {
                return RestResult.fail("未查询到账号信息");
            }
            mid = account.getMid();
        } else {
            TMarketMany many = marketManyRepository.find(asid);
            if (null == many) {
                return RestResult.fail("未查询到子账号信息");
            }
            mid = many.getMid();
        }
        TMarketStandard standard = new TMarketStandard();
        standard.setGid(gid);
        standard.setSid(sid);
        standard.setMid(mid);
        standard.setAid(aid);
        standard.setAsid(asid);
        standard.setCid(cid);
        standard.setCode(code);
        standard.setName(name);
        standard.setRemark(remark);
        standard.setPrice(price);
        if (!marketStandardRepository.update(standard)) {
            return RestResult.fail("修改对接标品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delMarketStandard(int id, int gid, int sid, int aid, int asid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!marketStandardRepository.delete(sid, aid, asid, cid)) {
            return RestResult.fail("删除对接标品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketStandard(int id, int gid, int page, int limit, int sid, int aid, int asid, String search) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        int total = marketStandardRepository.total(sid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketStandardRepository.pagination(sid, aid, asid, page, limit, search);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }

        // 属性
        val datas = new ArrayList<HashMap<String, Object>>();
        for (TMarketStandard c : list) {
            int cid = c.getCid();
            Integer sid2 = c.getSid();
            Integer aid2 = c.getAid();
            Integer asid2 = c.getAsid();
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("cid", cid);
            tmp.put("sid", sid2);
            tmp.put("mid", c.getMid());
            tmp.put("aid", aid2);
            tmp.put("asid", asid2);
            tmp.put("mcode", c.getCode());
            tmp.put("mname", c.getName());
            tmp.put("mremark", c.getRemark());
            tmp.put("alarm", c.getPrice());
            datas.add(tmp);

            if (null != sid2) {
                // 仓库
                TStorage storage = storageRepository.find(sid2);
                if (null != storage) {
                    tmp.put("storage", storage.getName());
                }
            }

            // 平台账号
            if (null != aid2) {
                TMarketAccount account = marketAccountRepository.find(aid2);
                if (null != account) {
                    tmp.put("account", account.getAccount());
                }
            }

            // 商品信息
            val standard = standardRepository.find(cid);
            if (null != standard) {
                tmp.put("category", standard.getCid());
                tmp.put("ccode", standard.getCode());
                tmp.put("cname", standard.getName());
                tmp.put("cremark", standard.getRemark());
            }

            // 商品属性
            val attrs = standardAttrRepository.find(cid);
            if (null != attrs && !attrs.isEmpty()) {
                val tmp2 = new ArrayList<String>();
                tmp.put("attrs", tmp2);
                for (TStandardAttr attr : attrs) {
                    tmp2.add(attr.getValue());
                }
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult setMarketCommodityDetail(int id, int gid, TMarketCommodityDetail detail) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        //
        int mid = 0;
        if (0 == detail.getAsid()) {
            TMarketAccount account = marketAccountRepository.find(detail.getAid());
            if (null == account) {
                return RestResult.fail("未查询到账号信息");
            }
            mid = account.getMid();
        } else {
            TMarketMany many = marketManyRepository.find(detail.getAsid());
            if (null == many) {
                return RestResult.fail("未查询到子账号信息");
            }
            mid = many.getMid();
        }
        detail.setMid(mid);

        // 只处理当天信息
        Date today = dateUtil.getEndTime(dateUtil.addOneDay(new Date(), -8));
        if (detail.getCdate().before(today)) {
            return RestResult.fail("只能更新7日内销售信息");
        }

        if (null == detail.getId()) {
            if (!marketCommodityDetailRepository.insert(detail)) {
                return RestResult.fail("插入商品销售信息失败");
            }
        } else {
            if (!marketCommodityDetailRepository.update(detail)) {
                return RestResult.fail("更新信息失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delMarketCommodityDetail(int id, int gid, int did) {
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
        Date today = dateUtil.getEndTime(dateUtil.addOneDay(new Date(), -8));
        if (detail.getCdate().before(today)) {
            return RestResult.fail("只能更新7日内信息");
        }
        if (!marketCommodityDetailRepository.delete(did)) {
            return RestResult.fail("删除商品销售信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketCommodityDetail(int id, int gid, int page, int limit, int sid, int aid, int asid, Date date, String search) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        int total = marketCommodityDetailRepository.total(sid, aid, asid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketCommodityDetailRepository.pagination(sid, aid, asid, page, limit, date, search);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }

        // 属性
        val datas = new ArrayList<HashMap<String, Object>>();
        for (MyMarketCommodity c : list) {
            int cid = c.getCid();
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("cid", cid);
            tmp.put("mcode", c.getCode());
            tmp.put("mname", c.getName());
            tmp.put("mremark", c.getRemark());
            tmp.put("alarm", c.getAlarm());
            tmp.put("mprice", c.getPrice());
            tmp.put("mvalue", c.getValue());
            datas.add(tmp);

            // 商品信息
            val commodity = commodityRepository.find(cid);
            if (null != commodity) {
                tmp.put("category", commodity.getCid());
                tmp.put("ccode", commodity.getCode());
                tmp.put("cname", commodity.getName());
                tmp.put("cremark", commodity.getRemark());
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult setMarketStandardDetail(int id, int gid, TMarketStandardDetail detail) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        //
        int mid = 0;
        if (0 == detail.getAsid()) {
            TMarketAccount account = marketAccountRepository.find(detail.getAid());
            if (null == account) {
                return RestResult.fail("未查询到账号信息");
            }
            mid = account.getMid();
        } else {
            TMarketMany many = marketManyRepository.find(detail.getAsid());
            if (null == many) {
                return RestResult.fail("未查询到子账号信息");
            }
            mid = many.getMid();
        }
        detail.setMid(mid);

        // 只处理当天信息
        Date today = dateUtil.getEndTime(dateUtil.addOneDay(new Date(), -8));
        if (detail.getCdate().before(today)) {
            return RestResult.fail("只能更新7日内销售信息");
        }

        if (null == detail.getId()) {
            if (!marketStandardDetailRepository.insert(detail)) {
                return RestResult.fail("插入商品销售信息失败");
            }
        } else {
            if (!marketStandardDetailRepository.update(detail)) {
                return RestResult.fail("更新信息失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delMarketStandardDetail(int id, int gid, int did) {
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
        Date today = dateUtil.getEndTime(dateUtil.addOneDay(new Date(), -8));
        if (detail.getCdate().before(today)) {
            return RestResult.fail("只能更新7日内信息");
        }
        if (!marketStandardDetailRepository.delete(did)) {
            return RestResult.fail("删除商品销售信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketStandardDetail(int id, int gid, int page, int limit, int sid, int aid, int asid, Date date, String search) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        int total = marketStandardDetailRepository.total(sid, aid, asid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketStandardDetailRepository.pagination(sid, aid, asid, page, limit, date, search);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }

        // 属性
        val datas = new ArrayList<HashMap<String, Object>>();
        for (MyMarketCommodity c : list) {
            int cid = c.getCid();
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("cid", cid);
            tmp.put("mcode", c.getCode());
            tmp.put("mname", c.getName());
            tmp.put("mremark", c.getRemark());
            tmp.put("alarm", c.getAlarm());
            tmp.put("mprice", c.getPrice());
            tmp.put("mvalue", c.getValue());
            datas.add(tmp);

            // 商品信息
            val standard = standardRepository.find(cid);
            if (null != standard) {
                tmp.put("category", standard.getCid());
                tmp.put("ccode", standard.getCode());
                tmp.put("cname", standard.getName());
                tmp.put("cremark", standard.getRemark());
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult getMarketSaleDetail(int id, int gid, int sid, int aid, int asid, Date date) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        val list = marketCommodityDetailRepository.sale(sid, aid, asid, date);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }

        val list2 = marketStandardDetailRepository.sale(sid, aid, asid, date);
        if (null == list2) {
            return RestResult.fail("未查询到销售信息");
        }

        // 属性
        val datas = new ArrayList<HashMap<String, Object>>();
        for (MyMarketCommodity c : list) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("ctype", COMMODITY.getValue());
            tmp.put("cid", c.getCid());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("remark", c.getRemark());
            tmp.put("price", c.getPrice());
            tmp.put("value", c.getValue());
            datas.add(tmp);
        }

        for (MyMarketCommodity c : list2) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("ctype", STANDARD.getValue());
            tmp.put("cid", c.getCid());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("remark", c.getRemark());
            tmp.put("price", c.getPrice());
            tmp.put("value", c.getValue());
            datas.add(tmp);
        }
        return RestResult.ok(new PageData(0, datas));
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
            return RestResult.ok(new PageData());
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
        return RestResult.ok(new PageData(total, datas));
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
            return RestResult.ok(new PageData());
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
        return RestResult.ok(new PageData(total, datas));
    }
}
