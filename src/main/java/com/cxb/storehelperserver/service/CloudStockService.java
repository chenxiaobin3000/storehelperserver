package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
    private CloudWeekRepository cloudWeekRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private CloudCommodityRepository cloudCommodityRepository;

    @Resource
    private PurchaseCommodityRepository purchaseCommodityRepository;

    @Resource
    private SaleCommodityRepository saleCommodityRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.stockday}")
    private int stockday;

    @Value("${store-app.config.stockspan}")
    private int stockspan;

    // 根据采购进货单修改库存
    public String handlePurchaseStock(TCloudOrder order, TPurchaseOrder purchase, boolean add) {
        val cloudCommodities = cloudCommodityRepository.find(order.getId());
        if (null == cloudCommodities || cloudCommodities.isEmpty()) {
            return "未查询到入库商品信息";
        }
        val purchaseCommodities = purchaseCommodityRepository.find(purchase.getId());
        if (null == purchaseCommodities || purchaseCommodities.isEmpty()) {
            return "未查询到采购商品信息";
        }
        int gid = order.getGid();
        int sid = order.getSid();
        for (TCloudCommodity cloudCommodity : cloudCommodities) {
            boolean find = false;
            for (TPurchaseCommodity purchaseCommodity : purchaseCommodities) {
                if (cloudCommodity.getCtype().equals(purchaseCommodity.getCtype())
                        && cloudCommodity.getCid().equals(purchaseCommodity.getCid())) {
                    if (!addStockCommodityP(gid, sid, cloudCommodity, purchaseCommodity, add)) {
                        return "添加库存信息失败";
                    }
                    find = true;
                    break;
                }
            }
            if (!find) {
                return "未查询到入库商品的采购信息";
            }
        }
        return null;
    }

    // 根据采购进货单修改库存
    public String handleAgreementStock(TCloudOrder order, TAgreementOrder shipped, boolean add) {
        val cloudCommodities = cloudCommodityRepository.find(order.getId());
        if (null == cloudCommodities || cloudCommodities.isEmpty()) {
            return "未查询到入库商品信息";
        }
        val agreementCommodities = agreementCommodityRepository.find(shipped.getId());
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return "未查询到履约商品信息";
        }
        int gid = order.getGid();
        int sid = order.getSid();
        for (TCloudCommodity cloudCommodity : cloudCommodities) {
            boolean find = false;
            for (TAgreementCommodity agreementCommodity : agreementCommodities) {
                if (cloudCommodity.getCtype().equals(agreementCommodity.getCtype())
                        && cloudCommodity.getCid().equals(agreementCommodity.getCid())) {
                    if (!addStockCommodityA(gid, sid, cloudCommodity, agreementCommodity, add)) {
                        return "添加库存信息失败";
                    }
                    find = true;
                    break;
                }
            }
            if (!find) {
                return "未查询到入库商品的采购信息";
            }
        }
        return null;
    }

    // 根据损耗单/退采购/退仓库修改库存
    public String handleLossStock(TCloudOrder order, boolean add) {
        val cloudCommodities = cloudCommodityRepository.find(order.getId());
        if (null == cloudCommodities || cloudCommodities.isEmpty()) {
            return "未查询到调度商品信息";
        }
        int sid = order.getSid();
        for (TCloudCommodity cloudCommodity : cloudCommodities) {
            int ctype = cloudCommodity.getCtype();
            int cid = cloudCommodity.getCid();
            TCloudStock stock = cloudStockRepository.find(sid, ctype, cid);
            if (null == stock) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "未查询到库存类型:" + ctype + ",商品:" + cid;
            }
            if (add) {
                // 重新计算库存价格
                int newValue = cloudCommodity.getValue(); // 入库单总重量
                BigDecimal newPrice = cloudCommodity.getPrice().multiply(new BigDecimal(newValue)); // 入库单总价
                BigDecimal oldPrice = stock.getPrice().multiply(new BigDecimal(stock.getValue())); // 库存总价
                BigDecimal allPrice = newPrice.add(oldPrice);
                int value = stock.getValue() + newValue;
                stock.setValue(value);
                stock.setPrice(allPrice.divide(new BigDecimal(value), 2, RoundingMode.DOWN));
            } else {
                int value = stock.getValue() - cloudCommodity.getValue();
                if (value < 0) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "库存商品数量不足:" + ctype + ",商品:" + cid;
                }
                stock.setValue(value);
            }
            if (!cloudStockRepository.update(stock)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "添加库存信息失败";
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
        int sid = order.getSid();
        for (TSaleCommodity saleCommodity : saleCommodities) {
            int ctype = saleCommodity.getCtype();
            int cid = saleCommodity.getCid();
            TCloudStock stock = cloudStockRepository.find(sid, ctype, cid);
            if (null == stock) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "未查询到库存类型:" + ctype + ",商品:" + cid;
            }
            if (add) {
                // 重新计算库存价格
                int newValue = saleCommodity.getValue(); // 入库单总重量
                BigDecimal newPrice = saleCommodity.getPrice().multiply(new BigDecimal(newValue)); // 入库单总价
                BigDecimal oldPrice = stock.getPrice().multiply(new BigDecimal(stock.getValue())); // 库存总价
                BigDecimal allPrice = newPrice.add(oldPrice);
                int value = stock.getValue() + newValue;
                stock.setValue(value);
                stock.setPrice(allPrice.divide(new BigDecimal(value), 2, RoundingMode.DOWN));
            } else {
                int value = stock.getValue() - saleCommodity.getValue();
                if (value < 0) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return "库存商品数量不足:" + ctype + ",商品:" + cid;
                }
                stock.setValue(value);
            }
            if (!cloudStockRepository.update(stock)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "添加库存信息失败";
            }
        }
        return null;
    }

    private boolean addStockCommodityP(int gid, int sid, TCloudCommodity cloudCommodity, TPurchaseCommodity purchaseCommodity, boolean add) {
        int newValue = cloudCommodity.getValue() * purchaseCommodity.getUnit(); // 入库单总重量
        TCloudStock stock = cloudStockRepository.find(sid, cloudCommodity.getCtype(), cloudCommodity.getCid());
        if (null == stock) {
            if (!add) {
                log.warn("未查询到要扣减的云仓库存:" + cloudCommodity.getOid() + ",类型:" + cloudCommodity.getCtype() + ",商品:" + cloudCommodity.getCid());
                return false;
            }
            stock = new TCloudStock();
            stock.setGid(gid);
            stock.setSid(sid);
            stock.setCtype(cloudCommodity.getCtype());
            stock.setCid(cloudCommodity.getCid());
            stock.setValue(newValue);
            stock.setPrice(purchaseCommodity.getPrice().divide(new BigDecimal(newValue), 2, RoundingMode.DOWN));
            return cloudStockRepository.insert(stock);
        } else {
            BigDecimal newPrice = purchaseCommodity.getPrice().multiply(new BigDecimal(newValue)); // 入库单总价
            BigDecimal oldPrice = stock.getPrice().multiply(new BigDecimal(stock.getValue())); // 库存总价
            BigDecimal allPrice = newPrice.add(oldPrice);
            int value = add ? stock.getValue() + newValue : stock.getValue() - newValue; // 重量直接想加
            if (value < 0) {
                log.warn("云仓库存商品扣减小于0:" + cloudCommodity.getOid() + ",类型:" + cloudCommodity.getCtype() + ",商品:" + cloudCommodity.getCid());
                return false;
            }
            stock.setValue(value);
            stock.setPrice(allPrice.divide(new BigDecimal(value), 2, RoundingMode.DOWN));
            return cloudStockRepository.update(stock);
        }
    }

    private boolean addStockCommodityA(int gid, int sid, TCloudCommodity cloudCommodity, TAgreementCommodity agreementCommodity, boolean add) {
        int newValue = cloudCommodity.getValue() * agreementCommodity.getUnit(); // 入库单总重量
        TCloudStock stock = cloudStockRepository.find(sid, cloudCommodity.getCtype(), cloudCommodity.getCid());
        if (null == stock) {
            if (!add) {
                log.warn("未查询到要扣减的云仓库存:" + cloudCommodity.getOid() + ",类型:" + cloudCommodity.getCtype() + ",商品:" + cloudCommodity.getCid());
                return false;
            }
            stock = new TCloudStock();
            stock.setGid(gid);
            stock.setSid(sid);
            stock.setCtype(cloudCommodity.getCtype());
            stock.setCid(cloudCommodity.getCid());
            stock.setValue(newValue);
            stock.setPrice(agreementCommodity.getPrice().divide(new BigDecimal(newValue), 2, RoundingMode.DOWN));
            return cloudStockRepository.insert(stock);
        } else {
            BigDecimal newPrice = agreementCommodity.getPrice().multiply(new BigDecimal(newValue)); // 入库单总价
            BigDecimal oldPrice = stock.getPrice().multiply(new BigDecimal(stock.getValue())); // 库存总价
            BigDecimal allPrice = newPrice.add(oldPrice);
            int value = add ? stock.getValue() + newValue : stock.getValue() - newValue; // 重量直接想加
            if (value < 0) {
                log.warn("云仓库存商品扣减小于0:" + cloudCommodity.getOid() + ",类型:" + cloudCommodity.getCtype() + ",商品:" + cloudCommodity.getCid());
                return false;
            }
            stock.setValue(value);
            stock.setPrice(allPrice.divide(new BigDecimal(value), 2, RoundingMode.DOWN));
            return cloudStockRepository.update(stock);
        }
    }
}
