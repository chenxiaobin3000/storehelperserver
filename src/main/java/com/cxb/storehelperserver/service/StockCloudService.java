package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import com.cxb.storehelperserver.service.model.PageData;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * desc: 库存统计业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StockCloudService {
    @Resource
    private CheckService checkService;

    @Resource
    private StockCloudRepository stockRepository;

    @Resource
    private StockCloudDayRepository stockDayRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private SaleCommodityRepository saleCommodityRepository;

    @Resource
    private CommodityStorageRepository commodityStorageRepository;

    @Resource
    private CommodityAttrRepository commodityAttrRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.stockday}")
    private int stockday;

    public TStockCloudDay getStockCommodity(int gid, int sid, int aid, int asid, int cid) {
        Date today = dateUtil.getStartTime(new Date());
        Date yesterday = dateUtil.addOneDay(today, -1);
        Date tomorrow = dateUtil.addOneDay(today, 1);
        TStockCloudDay day = stockDayRepository.find(sid, aid, asid, cid, yesterday);
        if (null == day) {
            day = new TStockCloudDay();
            day.setPrice(new BigDecimal(0));
            day.setWeight(0);
            day.setValue(0);
        }
        val commodities = stockRepository.findHistory(gid, sid, aid, asid, cid, today, tomorrow);
        if (null != commodities && !commodities.isEmpty()) {
            for (MyStockCommodity c : commodities) {
                day.setPrice(day.getPrice().add(c.getPrice()));
                day.setWeight(day.getWeight() + c.getWeight());
                day.setValue(day.getValue() + c.getValue());
            }
        }
        return day;
    }

    public RestResult getStockList(int id, int sid, int aid, int asid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 当日数据取库存
        val data = new HashMap<String, Object>();
        date = dateUtil.getStartTime(date);
        Date today = dateUtil.getStartTime(new Date());
        if (today.equals(date)) {
            // 昨日数据
            int total = commodityStorageRepository.total(sid, search);
            if (0 == total) {
                return RestResult.ok(new PageData());
            }
            Date yesterday = dateUtil.addOneDay(today, -1);
            val commodities = stockDayRepository.paginationAll(sid, aid, asid, page, limit, yesterday, search);
            if (null == commodities) {
                return RestResult.fail("获取商品信息失败");
            }
            // 加上今日变化量
            Date tomorrow = dateUtil.addOneDay(today, 1);
            val commodities2 = stockRepository.findHistoryAll(gid, sid, aid, asid, today, tomorrow);
            if (null != commodities2 && !commodities2.isEmpty()) {
                for (MyStockCommodity c : commodities) {
                    if (null == c.getPrice()) {
                        c.setPrice(new BigDecimal(0));
                    }
                    if (null == c.getWeight()) {
                        c.setWeight(0);
                    }
                    if (null == c.getValue()) {
                        c.setValue(0);
                    }
                    for (MyStockCommodity c2 : commodities2) {
                        if (c2.getCid().equals(c.getCid())) {
                            c.setPrice(c.getPrice().add(c2.getPrice()));
                            c.setWeight(c.getWeight() + c2.getWeight());
                            c.setValue(c.getValue() + c2.getValue());
                            break;
                        }
                    }
                }
            }
            data.put("total", total);
            data.put("list", commodities);
        } else {
            // 往期数据取快照
            int total = commodityStorageRepository.total(sid, search);
            if (0 == total) {
                return RestResult.ok(new PageData());
            }
            val commodities = stockDayRepository.paginationAll(sid, aid, asid, page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取商品信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        }
        return RestResult.ok(data);
    }

    public RestResult getTodayStockList(int id, int sid, int aid, int asid, int page, int limit, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 昨日数据
        val data = new HashMap<String, Object>();
        Date today = dateUtil.getStartTime(new Date());
        Date yesterday = dateUtil.addOneDay(today, -1);
        int total = stockDayRepository.total(sid, aid, asid, yesterday, search);
        List<HashMap<String, Object>> list = new ArrayList<>();
        if (0 != total) {
            val commodities = stockDayRepository.pagination(sid, aid, asid, page, limit, yesterday, search);
            if (null == commodities) {
                return RestResult.fail("获取商品信息失败");
            }
            for (MyStockCommodity c : commodities) {
                val tmp = new HashMap<String, Object>();
                list.add(tmp);
                tmp.put("id", c.getCid());
                tmp.put("code", c.getCode());
                tmp.put("name", c.getName());
                tmp.put("cid", c.getCtid());
                tmp.put("remark", c.getRemark());
                tmp.put("price", c.getPrice());
                tmp.put("weight", c.getWeight());
                tmp.put("value", c.getValue());

                // 属性
                List<TCommodityAttr> attrs = commodityAttrRepository.find(c.getCid());
                if (null != attrs && !attrs.isEmpty()) {
                    val tmp2 = new ArrayList<String>();
                    tmp.put("attrs", tmp2);
                    for (TCommodityAttr attr : attrs) {
                        tmp2.add(attr.getValue());
                    }
                }
            }
        }

        // 加上今日变化量
        Date tomorrow = dateUtil.addOneDay(today, 1);
        val commodities2 = stockRepository.findHistoryAll(gid, sid, aid, asid, today, tomorrow);
        if (null != commodities2 && !commodities2.isEmpty()) {
            for (MyStockCommodity c2 : commodities2) {
                boolean find = false;
                for (val c : list) {
                    if (c2.getCid().equals(c.get("id"))) {
                        c.put("price", c2.getPrice().add((BigDecimal) c.get("price")));
                        c.put("weight", c2.getWeight() + (int) c.get("weight"));
                        c.put("value", c2.getValue() + (int) c.get("value"));
                        find = true;
                        break;
                    }
                }
                if (!find && c2.getWeight() > 0) {
                    if (null != search) {
                        if (!c2.getName().contains(search)) {
                            continue;
                        }
                    }
                    val tmp = new HashMap<String, Object>();
                    list.add(tmp);
                    tmp.put("id", c2.getCid());
                    tmp.put("code", c2.getCode());
                    tmp.put("name", c2.getName());
                    tmp.put("cid", c2.getCtid());
                    tmp.put("remark", c2.getRemark());
                    tmp.put("price", c2.getPrice());
                    tmp.put("weight", c2.getWeight());
                    tmp.put("value", c2.getValue());

                    // 属性
                    List<TCommodityAttr> attrs = commodityAttrRepository.find(c2.getCid());
                    if (null != attrs && !attrs.isEmpty()) {
                        val tmp2 = new ArrayList<String>();
                        tmp.put("attrs", tmp2);
                        for (TCommodityAttr attr : attrs) {
                            tmp2.add(attr.getValue());
                        }
                    }
                    total = total + 1;
                }
            }
        }

        data.put("total", total);
        data.put("list", list);
        return RestResult.ok(data);
    }

    public RestResult getStockDay(int id, int gid, int sid, int aid, int asid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        SimpleDateFormat dateFormat = dateUtil.getSimpleDateFormat();
        Date end = dateUtil.getStartTime(new Date());
        Date start = dateUtil.addOneDay(end, -6);
        List<MyStockReport> stocks = stockDayRepository.findReport(gid, sid, aid, asid, start, end);
        if (null == stocks) {
            return RestResult.fail("未查询到库存信息");
        }
        val data = new HashMap<String, Object>();
        val list = new ArrayList<>();
        for (MyStockReport stock : stocks) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", stock.getId());
            tmp.put("total", stock.getTotal());
            tmp.put("date", dateFormat.format(stock.getCdate()));
            list.add(tmp);
        }
        data.put("list", list);
        data.put("today", stockRepository.findReport(gid, sid, aid, asid, dateUtil.getStartTime(new Date()), end));
        return RestResult.ok(data);
    }

    public RestResult getStockWeek(int id, int gid, int sid, int aid, int asid) {
        return null;
    }

    // 按履约单修改库存
    public String handleAgreementStock(TAgreementOrder order, boolean add) {
        val storageCommodities = agreementCommodityRepository.find(order.getId());
        if (null == storageCommodities || storageCommodities.isEmpty()) {
            return "未查询到商品信息";
        }
        int gid = order.getGid();
        int sid = order.getSid();
        int aid = order.getAid();
        int asid = order.getAsid();
        for (TAgreementCommodity agreementCommodity : storageCommodities) {
            int cid = agreementCommodity.getCid();
            BigDecimal price = agreementCommodity.getPrice();
            int weight = agreementCommodity.getWeight();
            int value = agreementCommodity.getValue();
            if (!stockRepository.insert(gid, sid, aid, asid, order.getOtype(), order.getId(), cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, order.getApplyTime())) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    // 按销售单修改库存
    public String handleSaleStock(TSaleOrder order, boolean add) {
        val saleCommodities = saleCommodityRepository.find(order.getId());
        if (null == saleCommodities || saleCommodities.isEmpty()) {
            return "未查询到商品信息";
        }
        int gid = order.getGid();
        int sid = order.getSid();
        int aid = order.getAid();
        int asid = order.getAsid();
        for (TSaleCommodity saleCommodity : saleCommodities) {
            int cid = saleCommodity.getCid();
            BigDecimal price = saleCommodity.getPrice();
            int weight = saleCommodity.getWeight();
            int value = saleCommodity.getValue();
            if (!stockRepository.insert(gid, sid, aid, asid, order.getOtype(), order.getId(), cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, order.getApplyTime())) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    // 计算库存
    public void countStockDay(int gid, int sid, int aid, int asid, Date date) {
        // 获取注册仓库的商品id
        val ids = new ArrayList<Integer>();
        val commodityStorages = commodityStorageRepository.findBySid(sid);
        for (TCommodityStorage c : commodityStorages) {
            ids.add(c.getCid());
        }

        MyStockCommodity yesterday = new MyStockCommodity();
        Date start = dateUtil.addOneDay(date, -stockday);
        Date stop = dateUtil.addOneDay(date, -1);
        // 获取历史记录，没有就补
        for (int cid : ids) {
            // 查找商品在周期内的销售数据, 没数据的直接忽略, 默认没有库存
            val commodities = stockRepository.findHistory(gid, sid, aid, asid, cid, start, date);
            if (null == commodities || commodities.isEmpty()) {
                continue;
            }
            Date tmp = dateUtil.getStartTime(start);
            yesterday.setPrice(new BigDecimal(0));
            yesterday.setWeight(0);
            yesterday.setValue(0);
            while (tmp.before(stop)) {
                // 已有数据就忽略
                val day = stockDayRepository.find(sid, aid, asid, cid, tmp);
                if (null != day) {
                    yesterday.setPrice(day.getPrice());
                    yesterday.setWeight(day.getWeight());
                    yesterday.setValue(day.getValue());
                    tmp = dateUtil.addOneDay(tmp, 1);
                    continue;
                }
                // 昨日库存数 + 当日所有库存
                for (MyStockCommodity c : commodities) {
                    if (c.getDate().equals(tmp)) {
                        yesterday.setPrice(yesterday.getPrice().add(c.getPrice()));
                        yesterday.setWeight(yesterday.getWeight() + c.getWeight());
                        yesterday.setValue(yesterday.getValue() + c.getValue());
                    }
                }
                // 添加库存数据
                stockDayRepository.insert(gid, sid, aid, asid, cid, yesterday.getPrice(), yesterday.getWeight(), yesterday.getValue(), tmp);
                tmp = dateUtil.addOneDay(tmp, 1);
            }
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
