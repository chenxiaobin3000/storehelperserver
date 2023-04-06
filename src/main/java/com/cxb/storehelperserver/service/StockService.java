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
    private CheckService checkService;

    @Resource
    private StockRepository stockRepository;

    @Resource
    private StockDayRepository stockDayRepository;

    @Resource
    private StockMonthRepository stockMonthRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private ProductCommodityRepository productCommodityRepository;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;

    @Resource
    private CommodityStorageRepository commodityStorageRepository;

    @Resource
    private HalfgoodStorageRepository halfgoodStorageRepository;

    @Resource
    private OriginalStorageRepository originalStorageRepository;

    @Resource
    private StandardStorageRepository standardStorageRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.stockday}")
    private int stockday;

    private static final Object lock = new Object();

    public RestResult getStockList(int id, int sid, int ctype, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        val data = new HashMap<String, Object>();
        int total = stockDayRepository.total(group.getGid(), sid, ctype, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = stockDayRepository.pagination(group.getGid(), sid, page, limit, ctype, date, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    // 废弃
    public RestResult getStockDetail(int id, int sid, int ctype, int page, int limit, Date start, Date end, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        val data = new HashMap<String, Object>();
        int total = stockRepository.total(group.getGid(), sid, ctype, start, end, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = stockRepository.pagination(group.getGid(), sid, page, limit, ctype, start, end, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockDay(int id, int gid, int sid, int ctype) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        SimpleDateFormat dateFormat = dateUtil.getSimpleDateFormat();
        Date end = dateUtil.getStartTime(new Date());
        Date start = dateUtil.addOneDay(end, -6);
        List<MyStockReport> stocks = stockDayRepository.findReport(gid, sid, ctype, start, end);
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
        data.put("today", stockRepository.findReport(gid, sid, ctype, dateUtil.getStartTime(new Date()), end));
        if (list.isEmpty()) {
            if (0 == sid) {
                int total = storageRepository.total(gid, null);
                if (0 != total) {
                    val list2 = storageRepository.pagination(gid, 1, total, null);
                    if (null != list2 && !list2.isEmpty()) {
                        for (TStorage s : list2) {
                            countStockDay(gid, s.getId(), ctype, end);
                        }
                    }
                }
            } else {
                countStockDay(gid, sid, ctype, end);
            }
        }
        return RestResult.ok(data);
    }

    public RestResult getStockWeek(int id, int gid, int sid, int ctype) {
        return null;
    }

    // 根据采购进货单/调度进货/履约退货修改库存
    public String handlePurchaseStock(TStorageOrder order, boolean add) {
        val storageCommodities = storageCommodityRepository.find(order.getId());
        if (null == storageCommodities || storageCommodities.isEmpty()) {
            return "未查询到商品信息";
        }
        Date cdate = new Date();
        int gid = order.getGid();
        int sid = order.getSid();
        for (TStorageCommodity storageCommodity : storageCommodities) {
            int ctype = storageCommodity.getCtype();
            int cid = storageCommodity.getCid();
            BigDecimal price = storageCommodity.getPrice();
            int weight = storageCommodity.getWeight();
            int value = storageCommodity.getValue();
            if (!stockRepository.insert(gid, sid, order.getOtype(), order.getOid(), ctype, cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, cdate)) {
                log.warn("增加库存明细信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    // 根据调度单/损耗单修改库存
    public String handleStorageStock(TStorageOrder order, boolean add) {
        val storageCommodities = storageCommodityRepository.find(order.getId());
        if (null == storageCommodities || storageCommodities.isEmpty()) {
            return "未查询到商品信息";
        }
        Date cdate = new Date();
        int gid = order.getGid();
        int sid = order.getSid();
        for (TStorageCommodity storageCommodity : storageCommodities) {
            int ctype = storageCommodity.getCtype();
            int cid = storageCommodity.getCid();
            BigDecimal price = storageCommodity.getPrice();
            int weight = storageCommodity.getWeight();
            int value = storageCommodity.getValue();
            if (!stockRepository.insert(gid, sid, order.getOtype(), order.getOid(), ctype, cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, cdate)) {
                log.warn("增加库存明细信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    // 根据生产单修改库存
    public String handleProductStock(TProductOrder order, boolean add) {
        val productCommodities = productCommodityRepository.find(order.getId());
        if (null == productCommodities || productCommodities.isEmpty()) {
            return "未查询到商品信息";
        }
        Date cdate = new Date();
        int gid = order.getGid();
        int sid = order.getSid();
        for (TProductCommodity productCommodity : productCommodities) {
            int ctype = productCommodity.getCtype();
            int cid = productCommodity.getCid();
            BigDecimal price = productCommodity.getPrice();
            int weight = productCommodity.getWeight();
            int value = productCommodity.getValue();
            if (!stockRepository.insert(gid, sid, order.getOtype(), order.getPid(), ctype, cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, cdate)) {
                log.warn("增加库存明细信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    // 根据完成单修改库存
    public String handleCompleteStock(TProductOrder order, boolean add) {
        val productCommodities = productCommodityRepository.find(order.getId());
        if (null == productCommodities || productCommodities.isEmpty()) {
            return "未查询到商品信息";
        }
        Date cdate = new Date();
        int gid = order.getGid();
        int sid = order.getSid();
        for (TProductCommodity productCommodity : productCommodities) {
            int ctype = productCommodity.getCtype();
            int cid = productCommodity.getCid();
            BigDecimal price = productCommodity.getPrice();
            int weight = productCommodity.getWeight();
            int value = productCommodity.getValue();
            if (!stockRepository.insert(gid, sid, order.getOtype(), order.getPid(), ctype, cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, cdate)) {
                log.warn("增加库存明细信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    // 根据履约单修改库存
    public String handleAgreementStock(TAgreementOrder order, boolean add) {
        val agreementCommodities = agreementCommodityRepository.find(order.getId());
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return "未查询到商品信息";
        }
        Date cdate = new Date();
        int gid = order.getGid();
        int sid = order.getSid();
        for (TAgreementCommodity agreementCommodity : agreementCommodities) {
            int ctype = agreementCommodity.getCtype();
            int cid = agreementCommodity.getCid();
            BigDecimal price = agreementCommodity.getPrice();
            int weight = agreementCommodity.getWeight();
            int value = agreementCommodity.getValue();
            if (!stockRepository.insert(gid, sid, order.getOtype(), order.getRid(), ctype, cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, cdate)) {
                log.warn("增加库存明细信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
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

    // 计算库存
    private void countStockDay(int gid, int sid, int ctype, Date date) {
        // 获取注册仓库的商品id
        val ids = new ArrayList<Integer>();
        switch (CommodityType.valueOf(ctype)) {
            case COMMODITY:
                val commodityStorages = commodityStorageRepository.findBySid(sid);
                for (TCommodityStorage c : commodityStorages) {
                    ids.add(c.getCid());
                }
                break;
            case HALFGOOD:
                val halfgoodStorages = halfgoodStorageRepository.findBySid(sid);
                for (THalfgoodStorage c : halfgoodStorages) {
                    ids.add(c.getCid());
                }
                break;
            case ORIGINAL:
                val originalStorages = originalStorageRepository.findBySid(sid);
                for (TOriginalStorage c : originalStorages) {
                    ids.add(c.getCid());
                }
                break;
            case STANDARD:
                val standardStorages = standardStorageRepository.findBySid(sid);
                for (TStandardStorage c : standardStorages) {
                    ids.add(c.getCid());
                }
                break;
            default:
                break;
        }

        MyStockCommodity yesterday = new MyStockCommodity();
        Date start = dateUtil.addOneDay(date, -stockday);
        synchronized (lock) {
            // 获取历史记录，没有就补
            for (int cid : ids) {
                val commodities = stockRepository.findHistory(gid, sid, ctype, cid, start, date);
                if (null == commodities || commodities.isEmpty()) {
                    continue;
                }
                Date tmp = start;
                yesterday.setPrice(new BigDecimal(0));
                yesterday.setWeight(0);
                yesterday.setValue(0);
                while (tmp.before(date)) {
                    val day = stockDayRepository.find(sid, ctype, cid, tmp);
                    if (null != day) {
                        continue;
                    }
                    boolean find = false;
                    for (MyStockCommodity c : commodities) {
                        if (c.getDate().equals(tmp)) {
                            yesterday.setPrice(yesterday.getPrice().add(c.getPrice()));
                            yesterday.setWeight(yesterday.getWeight() + c.getWeight());
                            yesterday.setValue(yesterday.getValue() + c.getValue());
                            stockDayRepository.insert(gid, sid, ctype, cid, yesterday.getPrice(), yesterday.getWeight(), yesterday.getValue(), tmp);

                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        stockDayRepository.insert(gid, sid, ctype, cid, yesterday.getPrice(), yesterday.getWeight(), yesterday.getValue(), tmp);
                    }
                    tmp = dateUtil.addOneDay(tmp, 1);
                }
            }
        }
    }

    // 计算库存月快照
    private void countStockMonth(int sid, int ctype, Date date) {

    }
}
