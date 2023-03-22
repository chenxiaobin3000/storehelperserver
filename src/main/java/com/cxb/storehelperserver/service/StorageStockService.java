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

import static com.cxb.storehelperserver.util.Permission.admin;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.COMMODITY;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.HALFGOOD;

/**
 * desc: 库存统计业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageStockService {
    @Resource
    private CheckService checkService;

    @Resource
    private StockRepository stockRepository;

    @Resource
    private StockDetailRepository stockDetailRepository;

    @Resource
    private StockDayRepository stockDayRepository;

    @Resource
    private StockWeekRepository stockWeekRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private ProductCommodityRepository productCommodityRepository;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.stockday}")
    private int stockday;

    @Value("${store-app.config.stockspan}")
    private int stockspan;

    private static final Object lock = new Object();

    public RestResult getStockList(int id, int sid, int ctype, int page, int limit, String search) {
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
        int total = stockRepository.total(group.getGid(), sid, ctype, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = stockRepository.pagination(group.getGid(), sid, page, limit, ctype, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockDetail(int id, int sid, int ctype, int page, int limit, String search) {
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
        int total = stockDetailRepository.total(group.getGid(), sid, ctype, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = stockDetailRepository.pagination(group.getGid(), sid, page, limit, ctype, search);
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
        data.put("today", stockRepository.findReport(gid, sid, ctype));
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
            TStock stock = stockRepository.find(sid, ctype, cid);
            if (null == stock) {
                if (!add) {
                    log.warn("未查询到要扣减的仓储库存:" + storageCommodity.getOid() + ",类型:" + ctype + ",商品:" + cid);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "未查询到要扣减的仓储库存:" + storageCommodity.getOid() + ",类型:" + ctype + ",商品:" + cid;
                }
                if (!stockRepository.insert(gid, sid, ctype, cid, price, weight, value)) {
                    log.warn("添加库存信息失败");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "添加库存信息失败";
                }
            } else {
                int newWeight = add ? stock.getWeight() + weight : stock.getWeight() - weight;
                if (newWeight < 0) {
                    log.warn("库存商品重量不足:" + ctype + ",商品:" + cid);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "库存商品重量不足:" + ctype + ",商品:" + cid;
                }
                int newValue = add ? stock.getValue() + value : stock.getValue() - value;
                if (newValue < 0) {
                    log.warn("库存商品件数不足:" + ctype + ",商品:" + cid);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "库存商品件数不足:" + ctype + ",商品:" + cid;
                }
                stock.setPrice(add ? stock.getPrice().add(price) : stock.getPrice().subtract(price));
                stock.setWeight(newWeight);
                stock.setValue(newValue);
                if (!stockRepository.update(stock)) {
                    log.warn("修改库存信息失败");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "修改库存信息失败";
                }
            }
            if (!stockDetailRepository.insert(gid, sid, order.getOtype(), order.getOid(), ctype, cid,
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
            TStock stock = stockRepository.find(sid, ctype, cid);
            if (null == stock) {
                log.warn("未查询到库存类型:" + ctype + ",商品:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "未查询到库存类型:" + ctype + ",商品:" + cid;
            }
            int newWeight = add ? stock.getWeight() + weight : stock.getWeight() - weight;
            if (newWeight < 0) {
                log.warn("库存商品重量不足:" + ctype + ",商品:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "库存商品重量不足:" + ctype + ",商品:" + cid;
            }
            int newValue = add ? stock.getValue() + value : stock.getValue() - value;
            if (newValue < 0) {
                log.warn("库存商品件数不足:" + ctype + ",商品:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "库存商品件数不足:" + ctype + ",商品:" + cid;
            }
            stock.setPrice(add ? stock.getPrice().add(price) : stock.getPrice().subtract(price));
            stock.setWeight(newWeight);
            stock.setValue(newValue);
            if (!stockRepository.update(stock)) {
                log.warn("修改库存信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "修改库存信息失败";
            }
            if (!stockDetailRepository.insert(gid, sid, order.getOtype(), order.getOid(), ctype, cid,
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
            TStock stock = stockRepository.find(sid, ctype, cid);
            if (null == stock) {
                log.warn("未查询到库存类型:" + ctype + ",商品:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "未查询到库存类型:" + ctype + ",商品:" + cid;
            }
            int newWeight = add ? stock.getWeight() + weight : stock.getWeight() - weight;
            if (newWeight < 0) {
                log.warn("库存商品重量不足:" + ctype + ",商品:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "库存商品重量不足:" + ctype + ",商品:" + cid;
            }
            int newValue = add ? stock.getValue() + value : stock.getValue() - value;
            if (newValue < 0) {
                log.warn("库存商品件数不足:" + ctype + ",商品:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "库存商品件数不足:" + ctype + ",商品:" + cid;
            }
            stock.setPrice(add ? stock.getPrice().add(price) : stock.getPrice().subtract(price));
            stock.setWeight(newWeight);
            stock.setValue(newValue);
            if (!stockRepository.update(stock)) {
                log.warn("修改库存信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "修改库存信息失败";
            }
            if (!stockDetailRepository.insert(gid, sid, order.getOtype(), order.getPid(), ctype, cid,
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
            TStock stock = stockRepository.find(sid, ctype, cid);
            if (null == stock) {
                if (HALFGOOD.getValue() != ctype && COMMODITY.getValue() != ctype) {
                    log.warn("未查询到库存类型:" + ctype + ",商品:" + cid);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "未查询到库存类型:" + ctype + ",商品:" + cid;
                }
                if (!stockRepository.insert(gid, sid, ctype, cid, price, weight, value)) {
                    log.warn("添加库存信息失败");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "添加库存信息失败";
                }
            } else {
                int newWeight = add ? stock.getWeight() + weight : stock.getWeight() - weight;
                if (newWeight < 0) {
                    log.warn("库存商品重量不足:" + ctype + ",商品:" + cid);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "库存商品重量不足:" + ctype + ",商品:" + cid;
                }
                int newValue = add ? stock.getValue() + value : stock.getValue() - value;
                if (newValue < 0) {
                    log.warn("库存商品件数不足:" + ctype + ",商品:" + cid);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "库存商品件数不足:" + ctype + ",商品:" + cid;
                }
                stock.setPrice(add ? stock.getPrice().add(price) : stock.getPrice().subtract(price));
                stock.setWeight(newWeight);
                stock.setValue(newValue);
                if (!stockRepository.update(stock)) {
                    log.warn("修改库存信息失败");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "修改库存信息失败";
                }
            }
            if (!stockDetailRepository.insert(gid, sid, order.getOtype(), order.getPid(), ctype, cid,
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
            TStock stock = stockRepository.find(sid, ctype, cid);
            if (null == stock) {
                log.warn("未查询到库存类型:" + ctype + ",商品:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "未查询到库存类型:" + ctype + ",商品:" + cid;
            }
            int newWeight = add ? stock.getWeight() + weight : stock.getWeight() - weight;
            if (newWeight < 0) {
                log.warn("库存商品重量不足:" + ctype + ",商品:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "库存商品重量不足:" + ctype + ",商品:" + cid;
            }
            int newValue = add ? stock.getValue() + value : stock.getValue() - value;
            if (newValue < 0) {
                log.warn("库存商品件数不足:" + ctype + ",商品:" + cid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "库存商品件数不足:" + ctype + ",商品:" + cid;
            }
            stock.setPrice(add ? stock.getPrice().add(price) : stock.getPrice().subtract(price));
            stock.setWeight(newWeight);
            stock.setValue(newValue);
            if (!stockRepository.update(stock)) {
                log.warn("修改库存信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "修改库存信息失败";
            }
            if (!stockDetailRepository.insert(gid, sid, order.getOtype(), order.getRid(), ctype, cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, cdate)) {
                log.warn("增加库存明细信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    public RestResult countStock(int id, int gid, Date date) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, admin)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        date = dateUtil.getStartTime(date);
        synchronized (lock) {
            val stocks = stockRepository.all(gid);
            for (TStock stock : stocks) {
                stockDayRepository.insert(stock.getId(), stock.getGid(), stock.getSid(), stock.getCtype(),
                        stock.getCid(), stock.getPrice(), stock.getWeight(), stock.getValue(), date);
            }
        }
        return RestResult.ok();
    }

    public void delStock(int sid, Date date) {
        stockDayRepository.delete(sid, date);
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
