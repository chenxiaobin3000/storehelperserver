package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.*;
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
import java.util.*;

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
    private CheckService checkService;

    @Resource
    private StockRepository stockRepository;

    @Resource
    private StockDayRepository stockDayRepository;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;

    @Resource
    private CommodityStorageRepository commodityStorageRepository;

    @Resource
    private CommodityAttrRepository commodityAttrRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.stockday}")
    private int stockday;

    public TStockDay getStockCommodity(int gid, int sid, int cid) {
        Date today = dateUtil.getStartTime(new Date());
        Date yesterday = dateUtil.addOneDay(today, -1);
        Date tomorrow = dateUtil.addOneDay(today, 1);
        TStockDay day = stockDayRepository.find(sid, cid, yesterday);
        if (null == day) {
            day = new TStockDay();
            day.setPrice(new BigDecimal(0));
            day.setWeight(0);
            day.setValue(0);
        }
        val commodities = stockRepository.findHistory(gid, sid, cid, today, tomorrow);
        if (null != commodities && !commodities.isEmpty()) {
            for (MyStockCommodity c : commodities) {
                day.setPrice(day.getPrice().add(c.getPrice()));
                day.setWeight(day.getWeight() + c.getWeight());
                day.setNorm(c.getNorm());
                day.setValue(day.getValue() + c.getValue());
            }
        }
        return day;
    }

    public RestResult setStockList(int id, int sid, Date date, List<String> codes, List<String> names, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        Date today = dateUtil.getEndTime(dateUtil.addOneDay(new Date(), -1));
        if (date.after(today)) {
            return RestResult.fail("只能导入今日之前的数据");
        }

        TStorage storage = storageRepository.find(sid);
        if (null == storage) {
            return RestResult.fail("获取仓库信息失败");
        }
        if (!group.getGid().equals(storage.getGid())) {
            return RestResult.fail("只能获取本公司信息");
        }

        int size = codes.size();
        if (size != names.size() || size != prices.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("导入信息异常");
        }

        // 导入数据
        int gid = group.getGid();
        for (int i = 0; i < size; i++) {
            String code = codes.get(i);
            TCommodity c = commodityRepository.findByCode(code);
            if (null == c) {
                return RestResult.fail("未查询到商品：" + names.get(i));
            }
            if (!c.getName().equals(names.get(i))) {
                return RestResult.fail("商品编号与名称不匹配：" + names.get(i));
            }
            int cid = c.getId();
            val day = stockDayRepository.find(sid, cid, date);
            if (null == day) {
                if (!stockDayRepository.insert(gid, sid, cid, prices.get(i), weights.get(i), norms.get(i), values.get(i), date)) {
                    return RestResult.fail("导入商品失败：" + names.get(i));
                }
            }
        }
        return RestResult.ok();
    }

    public RestResult getStockList(int id, int sid, int page, int limit, Date date, String search) {
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
        int total = commodityStorageRepository.total(sid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        if (today.equals(date)) {
            // 昨日数据
            Date yesterday = dateUtil.addOneDay(today, -1);
            val commodities = stockDayRepository.paginationAll(sid, page, limit, yesterday, search);
            if (null == commodities) {
                return RestResult.fail("获取商品信息失败");
            }
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
            }
            // 加上今日变化量
            Date tomorrow = dateUtil.addOneDay(today, 1);
            val commodities2 = stockRepository.findHistoryAll(gid, sid, today, tomorrow);
            if (null != commodities2 && !commodities2.isEmpty()) {
                for (MyStockCommodity c : commodities) {
                    for (MyStockCommodity c2 : commodities2) {
                        if (c2.getCid().equals(c.getCid())) {
                            c.setPrice(c.getPrice().add(c2.getPrice()));
                            c.setWeight(c.getWeight() + c2.getWeight());
                            c.setValue(c.getValue() + c2.getValue());
                            if (null == c.getNorm()) {
                                c.setNorm(c2.getNorm());
                            }
                            break;
                        }
                    }
                }
            }
            data.put("total", total);
            data.put("list", commodities);
        } else {
            // 往期数据取快照
            val commodities = stockDayRepository.paginationAll(sid, page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取商品信息失败");
            }
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
            }
            data.put("total", total);
            data.put("list", commodities);
        }
        return RestResult.ok(data);
    }

    public RestResult getTodayStockList(int id, int sid, int page, int limit, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 昨日数据
        val data = new HashMap<String, Object>();
        Date today = dateUtil.getStartTime(new Date());
        Date yesterday = dateUtil.addOneDay(today, -1);
        int total = stockDayRepository.total(sid, yesterday, search);
        List<HashMap<String, Object>> list = new ArrayList<>();
        if (0 != total) {
            val commodities = stockDayRepository.pagination(sid, page, limit, yesterday, search);
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
                tmp.put("norm", c.getNorm());
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
        val commodities2 = stockRepository.findHistoryAll(group.getGid(), sid, today, tomorrow);
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
                    tmp.put("norm", c2.getNorm());
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

    public RestResult getStockDay(int id, int gid, int sid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        SimpleDateFormat dateFormat = dateUtil.getSimpleDateFormat();
        Date end = dateUtil.getStartTime(new Date());
        Date start = dateUtil.addOneDay(end, -6);
        List<MyStockReport> stocks = stockDayRepository.findReport(gid, sid, start, end);
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
        data.put("today", stockRepository.findReport(gid, sid, dateUtil.getStartTime(new Date()), end));
        return RestResult.ok(data);
    }

    public RestResult getStockWeek(int id, int gid, int sid) {
        return null;
    }

    // 修改库存
    public String handleStock(TStorageOrder order, boolean add) {
        int gid = order.getGid();
        int sid = order.getSid();
        val storageCommodities = storageCommodityRepository.find(order.getId());
        if (null == storageCommodities || storageCommodities.isEmpty()) {
            return "未查询到商品信息";
        }
        for (TStorageCommodity storageCommodity : storageCommodities) {
            int cid = storageCommodity.getCid();
            BigDecimal price = storageCommodity.getPrice();
            int weight = storageCommodity.getWeight();
            int value = storageCommodity.getValue();
            // 校验库存
            if (!add) {
                TStockDay stock = getStockCommodity(gid, sid, cid);
                if (null == stock) {
                    log.warn("查询库存明细信息失败:" + cid);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "查询库存明细信息失败";
                }
                if (stock.getWeight() < weight) {
                    log.warn("库存重量明细信息失败:" + cid + "," + stock.getWeight() + "," + weight);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "库存重量明细信息失败";
                }
                if (stock.getValue() < value) {
                    log.warn("库存件数明细信息失败:" + cid + "," + stock.getValue() + "," + value);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "库存件数明细信息失败";
                }
            }
            if (!stockRepository.insert(gid, sid, order.getOtype(), order.getId(), cid, add ? price : price.negate(),
                    add ? weight : -weight, storageCommodity.getNorm(), add ? value : -value, order.getApplyTime())) {
                log.warn("增加库存明细信息失败:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    // 计算库存
    public void countStockDay(int gid, int sid, Date date) {
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
            val commodities = stockRepository.findHistory(gid, sid, cid, start, date);
            if (null == commodities || commodities.isEmpty()) {
                continue;
            }
            Date tmp = dateUtil.getStartTime(start);
            yesterday.setPrice(new BigDecimal(0));
            yesterday.setWeight(0);
            yesterday.setNorm("");
            yesterday.setValue(0);
            while (tmp.before(stop)) {
                // 已有数据就忽略
                val day = stockDayRepository.find(sid, cid, tmp);
                if (null != day) {
                    yesterday.setPrice(day.getPrice());
                    yesterday.setWeight(day.getWeight());
                    yesterday.setNorm(day.getNorm());
                    yesterday.setValue(day.getValue());
                    tmp = dateUtil.addOneDay(tmp, 1);
                    continue;
                }
                // 昨日库存数 + 当日所有库存
                for (MyStockCommodity c : commodities) {
                    if (c.getDate().equals(tmp)) {
                        yesterday.setPrice(yesterday.getPrice().add(c.getPrice()));
                        yesterday.setWeight(yesterday.getWeight() + c.getWeight());
                        yesterday.setNorm(c.getNorm());
                        yesterday.setValue(yesterday.getValue() + c.getValue());
                    }
                }
                // 添加库存数据
                stockDayRepository.insert(gid, sid, cid, yesterday.getPrice(), yesterday.getWeight(), yesterday.getNorm(), yesterday.getValue(), tmp);
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
