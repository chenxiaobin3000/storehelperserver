package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.PURCHASE_PURCHASE_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.STORAGE_PURCHASE_IN_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.STORAGE_PURCHASE_OUT_ORDER;

/**
 * desc: 采购业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class PurchaseService {
    @Resource
    private CheckService checkService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private StorageService storageService;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseCommodityRepository purchaseCommodityRepository;

    @Resource
    private PurchaseAttachmentRepository purchaseAttachmentRepository;

    @Resource
    private PurchaseReturnRepository purchaseReturnRepository;

    @Resource
    private PurchaseStorageRepository purchaseStorageRepository;

    @Resource
    private ProductAgreementRepository productAgreementRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 采购仓储进货
     */
    public RestResult purchase(int id, TPurchaseOrder order, int sid, int review, int storage, List<Integer> commoditys, List<BigDecimal> prices,
                               List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_purchase, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成采购单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createPurchaseComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成采购单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!purchaseOrderRepository.insert(order)) {
            return RestResult.fail("生成采购订单失败");
        }
        int oid = order.getId();
        String msg = purchaseOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            ret = reviewPurchase(id, oid);
            if (RestResult.isOk(ret) && storage > 0) {
                // 一键入库
                if (sid <= 0) {
                    return RestResult.fail("未指定仓库，一键入库失败");
                }
                TStorageOrder storageOrder = new TStorageOrder();
                storageOrder.setOtype(STORAGE_PURCHASE_IN_ORDER.getValue());
                storageOrder.setSid(sid);
                storageOrder.setTid(0);
                storageOrder.setApply(order.getApply());
                storageOrder.setApplyTime(order.getApplyTime());
                return storageService.purchaseIn(id, storageOrder, oid, review, commoditys, weights, values, attrs);
            }
        }
        return ret;
    }

    /**
     * desc: 原料采购仓储修改
     */
    public RestResult setPurchase(int id, int oid, int supplier, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setSupplier(supplier);
        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_purchase, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成采购单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createPurchaseComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("生成采购订单失败");
        }
        String msg = purchaseOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delPurchase(int id, int oid) {
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
        }

        // 删除商品附件数据
        purchaseAttachmentRepository.deleteByOid(oid);
        if (!purchaseCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!purchaseOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewPurchase(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }
        return reviewService.review(order.getApply(), id, group.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokePurchase(int id, int oid) {
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }
        // 已入库或退货的不能撤销
        if (order.getUnit() > order.getCurUnit()) {
            return RestResult.fail("已入库或退货的订单不能撤销");
        }
        // 存在入库单就不能改
        if (null != purchaseStorageRepository.find(oid)) {
            return RestResult.fail("已入库的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_purchase_purchase);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!purchaseOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setPurchasePay(int id, int oid, BigDecimal pay) {
        // 校验审核人员信息
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        order.setPayPrice(pay);
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("更新已付款信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setPurchaseSupplier(int id, int oid, int sid) {
        // 校验审核人员信息
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        order.setSupplier(sid);
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("更新供应商信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 原料退货
     */
    public RestResult returnc(int id, TPurchaseOrder order, int rid, int sid, int review, int storage, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 采购单未审核不能退货
        TPurchaseOrder purchase = purchaseOrderRepository.find(rid);
        if (null == purchase) {
            return RestResult.fail("未查询到采购单");
        }
        if (!purchase.getOtype().equals(PURCHASE_PURCHASE_ORDER.getValue())) {
            return RestResult.fail("退货单据类型异常");
        }
        if (null == purchase.getReview()) {
            return RestResult.fail("采购单未审核通过，不能进行入库");
        }

        order.setGid(purchase.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_return, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createReturnComms(order, rid, commoditys, prices, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成退货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!purchaseOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        int oid = order.getId();
        String msg = purchaseOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加关联
        if (!purchaseReturnRepository.insert(oid, rid)) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            ret = reviewReturn(id, oid);
            if (RestResult.isOk(ret) && storage > 0) {
                // 一键出库
                if (sid <= 0) {
                    return RestResult.fail("未指定仓库，一键出库失败");
                }
                TStorageOrder storageOrder = new TStorageOrder();
                storageOrder.setOtype(STORAGE_PURCHASE_OUT_ORDER.getValue());
                storageOrder.setSid(sid);
                storageOrder.setTid(0);
                storageOrder.setApply(order.getApply());
                storageOrder.setApplyTime(order.getApplyTime());
                return storageService.purchaseOut(id, storageOrder, oid, review, commoditys, weights, values, attrs);
            }
        }
        return ret;
    }

    /**
     * desc: 原料退货修改
     */
    public RestResult setReturn(int id, int oid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        // 采购单
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到采购单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_return, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createReturnComms(order, pid, commoditys, prices, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        String msg = purchaseOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delReturn(int id, int oid) {
        // 删除关联
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到采购单信息");
        }
        if (!purchaseReturnRepository.delete(oid, pid)) {
            return RestResult.fail("删除采购退货信息失败");
        }
        return delPurchase(id, oid);
    }

    public RestResult reviewReturn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        order.setComplete(new Byte("1"));
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }
        // 存在出库单就不能改
        if (null != purchaseStorageRepository.find(oid)) {
            return RestResult.fail("已出库的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_purchase_return);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!purchaseOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TPurchaseOrder order, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(gid, reviewPerm, reviews);
    }

    private RestResult createPurchaseComms(TPurchaseOrder order, List<Integer> commoditys, List<BigDecimal> prices,
                                           List<Integer> weights, List<String> norms, List<Integer> values, List<TPurchaseCommodity> list) {
        // 生成采购单
        int size = commoditys.size();
        if (size != prices.size() || size != weights.size() || size != norms.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            TPurchaseCommodity c = new TPurchaseCommodity();
            c.setCid(commoditys.get(i));
            c.setPrice(prices.get(i));
            c.setWeight(weights.get(i));
            c.setNorm(norms.get(i));
            c.setValue(values.get(i));
            list.add(c);

            total = total + c.getWeight();
            price = price.add(c.getPrice());
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }

    private RestResult createReturnComms(TPurchaseOrder order, int rid, List<Integer> commoditys, List<BigDecimal> prices,
                                         List<Integer> weights, List<Integer> values, List<TPurchaseCommodity> list) {
        // 生成退货单
        int size = commoditys.size();
        if (size != prices.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val purchaseCommodities = purchaseCommodityRepository.find(rid);
        if (null == purchaseCommodities || purchaseCommodities.isEmpty()) {
            return RestResult.fail("未查询到采购商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TPurchaseCommodity pc : purchaseCommodities) {
                if (pc.getCid() == cid) {
                    find = true;
                    if (weight > pc.getWeight()) {
                        return RestResult.fail("退货商品重量不能大于采购重量, 商品id:" + cid);
                    }
                    if (value > pc.getValue()) {
                        return RestResult.fail("退货商品件数不能大于采购件数, 商品id:" + cid);
                    }

                    TPurchaseCommodity c = new TPurchaseCommodity();
                    c.setCid(cid);
                    c.setPrice(prices.get(i));
                    c.setWeight(weight);
                    c.setNorm(pc.getNorm());
                    c.setValue(value);
                    list.add(c);

                    total = total + c.getWeight();
                    price = price.add(c.getPrice());
                    break;
                }
            }
            if (!find) {
                return RestResult.fail("未查询到商品id:" + cid);
            }
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }

    // 获取退货对应的采购单
    private int getPurchaseId(int id) {
        TPurchaseReturn ret = purchaseReturnRepository.find(id);
        if (null != ret) {
            return ret.getPid();
        }
        return 0;
    }
}
