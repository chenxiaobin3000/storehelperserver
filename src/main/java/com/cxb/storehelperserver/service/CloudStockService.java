package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import com.cxb.storehelperserver.service.model.PageData;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.TypeDefine;
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

import static com.cxb.storehelperserver.util.Permission.admin;

/**
 * desc: 云仓库存统计业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CloudStockService {
    @Resource
    private CheckService checkService;

    @Resource
    private CloudStockRepository cloudStockRepository;

    @Resource
    private CloudDetailRepository cloudDetailRepository;

    @Resource
    private CloudDayRepository cloudDayRepository;

    @Resource
    private CloudMonthRepository cloudMonthRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private CloudCommodityRepository cloudCommodityRepository;

    @Resource
    private PurchaseCommodityRepository purchaseCommodityRepository;

    @Resource
    private SaleCommodityRepository saleCommodityRepository;

    @Resource
    private CommodityStorageRepository commodityStorageRepository;

    @Resource
    private StandardStorageRepository standardStorageRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private CloudRepository cloudRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.stockday}")
    private int stockday;

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
        int total = cloudStockRepository.total(group.getGid(), sid, ctype, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = cloudStockRepository.pagination(group.getGid(), sid, page, limit, ctype, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

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
        int total = cloudDetailRepository.total(group.getGid(), sid, ctype, start, end, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = cloudDetailRepository.pagination(group.getGid(), sid, page, limit, ctype, start, end, search);
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
        List<MyStockReport> stocks = cloudDayRepository.findReport(gid, sid, ctype, start, end);
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
        data.put("today", cloudStockRepository.findReport(gid, sid, ctype));
        if (list.isEmpty()) {
            countStockDay(sid, ctype, end);
        }
        return RestResult.ok(data);
    }

    public RestResult getStockWeek(int id, int gid, int sid, int ctype) {
        return null;
    }

    // 根据采购进货单/履约发货单修改库存
    public String handlePurchaseStock(TCloudOrder order, boolean add) {
        val cloudCommodities = cloudCommodityRepository.find(order.getId());
        if (null == cloudCommodities || cloudCommodities.isEmpty()) {
            return "未查询到入库商品信息";
        }
        Date cdate = new Date();
        int gid = order.getGid();
        int sid = order.getCid();
        for (TCloudCommodity cloudCommodity : cloudCommodities) {
            int ctype = cloudCommodity.getCtype();
            int cid = cloudCommodity.getCid();
            BigDecimal price = cloudCommodity.getPrice();
            int weight = cloudCommodity.getWeight();
            int value = cloudCommodity.getValue();
            TCloudStock stock = cloudStockRepository.find(sid, ctype, cid);
            if (null == stock) {
                if (!add) {
                    log.warn("未查询到要扣减的云仓库存:" + cloudCommodity.getOid() + ",类型:" + ctype + ",商品:" + cid);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "未查询到要扣减的云仓库存:" + cloudCommodity.getOid() + ",类型:" + ctype + ",商品:" + cid;
                }
                if (!cloudStockRepository.insert(gid, sid, ctype, cid, price, weight, value)) {
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
                if (!cloudStockRepository.update(stock)) {
                    log.warn("修改库存信息失败");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "修改库存信息失败";
                }
            }
            if (!cloudDetailRepository.insert(gid, sid, order.getOtype(), order.getOid(), ctype, cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, cdate)) {
                log.warn("增加库存明细信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    // 根据损耗单/退采购/退仓库修改库存
    public String handleCloudStock(TCloudOrder order, boolean add) {
        val cloudCommodities = cloudCommodityRepository.find(order.getId());
        if (null == cloudCommodities || cloudCommodities.isEmpty()) {
            return "未查询到商品信息";
        }
        Date cdate = new Date();
        int gid = order.getGid();
        int sid = order.getCid();
        for (TCloudCommodity cloudCommodity : cloudCommodities) {
            int ctype = cloudCommodity.getCtype();
            int cid = cloudCommodity.getCid();
            BigDecimal price = cloudCommodity.getPrice();
            int weight = cloudCommodity.getWeight();
            int value = cloudCommodity.getValue();
            TCloudStock stock = cloudStockRepository.find(sid, ctype, cid);
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
            if (!cloudStockRepository.update(stock)) {
                log.warn("修改库存信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "修改库存信息失败";
            }
            if (!cloudDetailRepository.insert(gid, sid, order.getOtype(), order.getOid(), ctype, cid,
                    add ? price : price.negate(), add ? weight : -weight, add ? value : -value, cdate)) {
                log.warn("增加库存明细信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "增加库存明细信息失败";
            }
        }
        return null;
    }

    // 根据销售售后单修改库存
    public String handleSaleStock(TSaleOrder order, boolean add) {
        val saleCommodities = saleCommodityRepository.find(order.getId());
        if (null == saleCommodities || saleCommodities.isEmpty()) {
            return "未查询到调度商品信息";
        }
        Date cdate = new Date();
        int gid = order.getGid();
        int sid = order.getSid();
        for (TSaleCommodity saleCommodity : saleCommodities) {
            int ctype = saleCommodity.getCtype();
            int cid = saleCommodity.getCid();
            BigDecimal price = saleCommodity.getPrice();
            int weight = saleCommodity.getWeight();
            int value = saleCommodity.getValue();
            TCloudStock stock = cloudStockRepository.find(sid, ctype, cid);
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
            if (!cloudStockRepository.update(stock)) {
                log.warn("修改库存信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "修改库存信息失败";
            }
            if (!cloudDetailRepository.insert(gid, sid, order.getOtype(), null, ctype, cid,
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

        TCloud cloud = cloudRepository.find(sid);
        if (null == cloud) {
            return RestResult.fail("获取云仓信息失败");
        }
        if (!group.getGid().equals(cloud.getGid())) {
            return RestResult.fail("只能获取本公司信息");
        }
        return null;
    }

    // 计算库存
    private void countStockDay(int sid, int ctype, Date date) {
        // 获取注册仓库的商品id
        val ids = new ArrayList<Integer>();
        switch (TypeDefine.CommodityType.valueOf(ctype)) {
            case COMMODITY:
                val commodityStorages = commodityStorageRepository.findBySid(sid);
                for (TCommodityStorage c : commodityStorages) {
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

        synchronized (lock) {
            // 获取历史记录，没有就补

            //
            val stocks = cloudStockRepository.all(sid);
            for (TCloudStock stock : stocks) {
                cloudDayRepository.insert(stock.getId(), stock.getGid(), stock.getSid(), stock.getCtype(),
                        stock.getCid(), stock.getPrice(), stock.getWeight(), stock.getValue(), date);
            }
        }
    }

    // 计算库存月快照
    private void countStockMonth(int sid, int ctype, Date date) {

    }
}
