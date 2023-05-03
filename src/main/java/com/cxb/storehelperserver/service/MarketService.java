package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
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
    private MarketAccountRepository marketAccountRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private CommodityAttrRepository commodityAttrRepository;

    @Resource
    private CommodityStorageRepository commodityStorageRepository;

    @Resource
    private CommodityOriginalRepository commodityOriginalRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private GroupMarketRepository groupMarketRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult addMarketAccount(int id, int gid, int mid, String account, String remark) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        if (!marketAccountRepository.insert(gid, mid, account, remark)) {
            return RestResult.fail("添加账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setMarketAccount(int id, int gid, int mid, int aid, String account, String remark) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        val accounts = marketAccountRepository.find(gid, mid);
        for (TMarketAccount marketAccount : accounts) {
            if (marketAccount.getId().equals(aid)) {
                marketAccount.setAccount(account);
                marketAccount.setRemark(remark);
                if (!marketAccountRepository.update(marketAccount)) {
                    return RestResult.fail("修改对接商品信息失败");
                }
                return RestResult.ok();
            }
        }
        return RestResult.fail("未查询到账号信息");
    }

    public RestResult delMarketAccount(int id, int gid, int mid, int aid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!marketAccountRepository.delete(aid)) {
            return RestResult.fail("删除账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketAccountList(int id, int gid, int mid, int page, int limit) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        int total = marketAccountRepository.total(gid, mid);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketAccountRepository.pagination(gid, mid, page, limit);
        if (null == list) {
            return RestResult.fail("未查询到账号信息");
        }
        val datas = new HashMap<>();
        datas.put("total", total);
        datas.put("list", list);
        return RestResult.ok(datas);
    }

    public RestResult getMarketAllAccount(int id, int gid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        int total = marketAccountRepository.total(gid, 0);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketAccountRepository.pagination(gid, 0, 1, total);
        if (null == list) {
            return RestResult.fail("未查询到账号信息");
        }
        val datas = new HashMap<>();
        datas.put("total", total);
        datas.put("list", list);
        return RestResult.ok(datas);
    }

    public RestResult setMarketCommodity(int id, int gid, int aid, int cid, String code, String name, String remark, BigDecimal price) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        TMarketAccount account = marketAccountRepository.find(aid);
        if (null == account) {
            return RestResult.fail("未查询到账号信息");
        }

        TMarketCommodity commodity = new TMarketCommodity();
        commodity.setGid(gid);
        commodity.setMid(account.getMid());
        commodity.setAid(aid);
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

    public RestResult delMarketCommodity(int id, int gid, int aid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!marketCommodityRepository.delete(aid, cid)) {
            return RestResult.fail("删除对接商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketCommodity(int id, int gid, int aid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        val ret = new HashMap<String, Object>();
        ret.put("commodity", marketCommodityRepository.find(aid, cid));
        return RestResult.ok(ret);
    }

    public RestResult getMarketCommodityList(int id, int gid, int page, int limit, int aid, String search) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        int total = marketCommodityRepository.total(aid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketCommodityRepository.pagination(aid, page, limit, search);
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
            tmp.put("cate", c.getCate());
            tmp.put("mid", c.getMid());
            tmp.put("mcode", c.getCode());
            tmp.put("mname", c.getName());
            tmp.put("mremark", c.getRemark());
            tmp.put("alarm", c.getPrice());
            datas.add(tmp);

            // 商品信息
            val commodity = commodityRepository.find(cid);
            if (null != commodity) {
                tmp.put("category", commodity.getCid());
                tmp.put("ccode", commodity.getCode());
                tmp.put("cname", commodity.getName());
                tmp.put("cremark", commodity.getRemark());
            }

            // 仓库
            val storages = commodityStorageRepository.find(cid);
            if (null != storages && !storages.isEmpty()) {
                val list2 = new ArrayList<HashMap<String, Object>>();
                tmp.put("storages", list2);
                for (TCommodityStorage ss : storages) {
                    val tmp2 = new HashMap<String, Object>();
                    int sid = ss.getSid();
                    tmp2.put("sid", sid);
                    TStorage storage = storageRepository.find(sid);
                    if (null != storage) {
                        tmp2.put("name", storage.getName());
                    }
                    list2.add(tmp2);
                }
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

            // 关联来源
            TCommodityOriginal commodityOriginal = commodityOriginalRepository.find(cid);
            if (null != commodityOriginal) {
                TCommodity original = commodityRepository.find(commodityOriginal.getOid());
                if (null != original) {
                    tmp.put("oname", original.getName());
                    tmp.put("ocode", original.getCode());
                }
            }

            // 账号
            val list2 = marketCommodityRepository.findByCid(cid);
            if (null != list2 && !list2.isEmpty()) {
                val list3 = new ArrayList<String>();
                for (TMarketCommodity c2 : list2) {
                    TMarketAccount account = marketAccountRepository.find(c2.getAid());
                    if (null != account) {
                        list3.add(account.getAccount());
                    }
                }
                tmp.put("accounts", list3);
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult setMarketCommodityList(int id, int gid, int sid, int aid, Date date, List<String> commoditys, List<BigDecimal> prices, List<Integer> values) {
        int size = commoditys.size();
        if (size != prices.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 模板数据
        TMarketAccount account = marketAccountRepository.find(aid);
        if (null == account) {
            return RestResult.fail("未查询到账号信息");
        }
        TMarketCommodityDetail detail = new TMarketCommodityDetail();
        detail.setGid(gid);
        detail.setMid(account.getMid());
        detail.setAid(aid);
        detail.setCdate(date);

        // 清空原有数据
        val ids = marketCommodityDetailRepository.findIds(sid, aid, date);
        if (null != ids && !ids.isEmpty()) {
            for (Integer v : ids) {
                marketCommodityDetailRepository.delete(v);
            }
        }

        // 获取商品列表
        val list = marketCommodityRepository.findAll(aid);
        if (null == list || list.isEmpty()) {
            return RestResult.fail("未查询到上架商品信息");
        }

        for (int i = 0; i < size; i++) {
            for (TMarketCommodity commodity : list) {
                if (commodity.getCode().equals(commoditys.get(i))) {
                    detail.setId(0);
                    detail.setCid(commodity.getCid());
                    detail.setPrice(prices.get(i));
                    detail.setValue(values.get(i));
                    if (!marketCommodityDetailRepository.insert(detail)) {
                        return RestResult.fail("插入商品销售信息失败");
                    }
                    break;
                }
            }
        }
        return RestResult.ok();
    }

    public RestResult setMarketCommodityDetail(int id, int gid, TMarketCommodityDetail detail) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        //
        TMarketAccount account = marketAccountRepository.find(detail.getAid());
        if (null == account) {
            return RestResult.fail("未查询到账号信息");
        }
        detail.setMid(account.getMid());

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

    public RestResult getMarketCommodityDetail(int id, int gid, int page, int limit, int sid, int aid, Date date, String search) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        int total = marketCommodityDetailRepository.total(sid, aid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketCommodityDetailRepository.pagination(sid, aid, page, limit, date, search);
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

    public RestResult getMarketSaleDetail(int id, int gid, int aid, Date date) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        val list = marketCommodityDetailRepository.sale(aid, date);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }

        // 属性
        val datas = new ArrayList<HashMap<String, Object>>();
        for (MyMarketCommodity c : list) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
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
}
