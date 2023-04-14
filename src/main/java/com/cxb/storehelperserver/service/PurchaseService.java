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
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.PURCHASE_PURCHASE_ORDER;

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
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseCommodityRepository purchaseCommodityRepository;

    @Resource
    private PurchaseAttachmentRepository purchaseAttachmentRepository;

    @Resource
    private PurchaseReturnRepository purchaseReturnRepository;

    @Resource
    private StoragePurchaseRepository storagePurchaseRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 采购仓储进货
     */
    public RestResult purchase(int id, TPurchaseOrder order, List<Integer> types, List<Integer> commoditys,
                               List<BigDecimal> prices, List<Integer> weights, List<Integer> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_purchase_apply, mp_purchase_purchase_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成采购单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createPurchaseComms(order, types, commoditys, prices, weights, norms, values, comms);
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
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 原料采购仓储修改
     */
    public RestResult setPurchase(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys,
                                  List<BigDecimal> prices, List<Integer> weights, List<Integer> norms, List<Integer> values, List<Integer> attrs) {
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

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_purchase_apply, mp_purchase_purchase_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 更新仓库信息
        if (!order.getSid().equals(sid)) {
            order.setSid(sid);
            ret = reviewService.update(order.getOtype(), oid, sid);
            if (null != ret) {
                return ret;
            }
        }

        // 生成采购单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createPurchaseComms(order, types, commoditys, prices, weights, norms, values, comms);
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

        // 校验是否订单提交人，已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("订单必须由申请人删除");
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
        int gid = group.getGid();

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
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
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

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, purchase_purchase)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_purchase_purchase_review);
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
            return RestResult.fail("未查询到要审核的订单");
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
    public RestResult returnc(int id, TPurchaseOrder order, List<Integer> types, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 采购单未审核，已入库都不能退货
        int rid = order.getRid();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(rid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到采购单");
        }
        if (!purchaseOrder.getOtype().equals(PURCHASE_PURCHASE_ORDER.getValue())) {
            return RestResult.fail("退货单据类型异常");
        }
        if (null == purchaseOrder.getReview()) {
            return RestResult.fail("采购单未审核通过，不能进行入库");
        }
        if (storagePurchaseRepository.checkByPid(rid)) {
            return RestResult.fail("采购商品已入库，请使用仓储退货单");
        }

        order.setGid(purchaseOrder.getGid());
        order.setSid(purchaseOrder.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_return_apply, mp_purchase_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createReturnComms(order, rid, types, commoditys, prices, weights, values, comms);
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
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 原料退货修改
     */
    public RestResult setReturn(int id, int oid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_return_apply, mp_purchase_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createReturnComms(order, order.getRid(), types, commoditys, prices, weights, values, comms);
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
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 校验是否订单提交人，已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("订单必须由申请人删除");
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

    public RestResult reviewReturn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 校验审核人员信息
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出采购单
        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getRid());
        if (null == purchase) {
            return RestResult.fail("未查询到对应的进货单");
        }
        int unit = purchase.getCurUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("退货商品总量不能超出采购订单总量");
        }
        BigDecimal price = purchase.getCurPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("退货商品总价不能超出采购订单总价");
        }
        if (0 == unit) {
            purchase.setComplete(new Byte("1"));
        }
        purchase.setCurUnit(unit);
        purchase.setCurPrice(price);
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改进货单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!purchaseReturnRepository.insert(oid, order.getRid())) {
            return RestResult.fail("添加采购退货信息失败");
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, purchase_return)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 还原扣除的采购单数量
        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getRid());
        if (null == purchase) {
            return RestResult.fail("未查询到对应的进货单");
        }
        purchase.setCurUnit(purchase.getCurUnit() + order.getUnit());
        purchase.setCurPrice(purchase.getCurPrice().add(order.getPrice()));
        purchase.setComplete(new Byte("0"));
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改进货单数据失败");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_purchase_return_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!purchaseOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!purchaseReturnRepository.delete(oid, order.getRid())) {
            return RestResult.fail("删除采购退货信息失败");
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TPurchaseOrder order, int applyPerm, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(id, gid, applyPerm, reviewPerm, reviews);
    }

    private RestResult createPurchaseComms(TPurchaseOrder order, List<Integer> types, List<Integer> commoditys, List<BigDecimal> prices,
                                           List<Integer> weights, List<Integer> norms, List<Integer> values, List<TPurchaseCommodity> list) {
        // 生成采购单
        int size = commoditys.size();
        if (size != types.size() || size != prices.size() || size != weights.size() || size != norms.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            switch (CommodityType.valueOf(ctype)) {
                case ORIGINAL:
                    TOriginal original = originalRepository.find(cid);
                    if (null == original) {
                        return RestResult.fail("未查询到原料：" + cid);
                    }
                    break;
                case STANDARD:
                    TStandard standard = standardRepository.find(cid);
                    if (null == standard) {
                        return RestResult.fail("未查询到标品：" + cid);
                    }
                    break;
                default:
                    return RestResult.fail("商品类型异常：" + ctype);
            }

            TPurchaseCommodity c = new TPurchaseCommodity();
            c.setCtype(ctype);
            c.setCid(cid);
            c.setPrice(prices.get(i));
            c.setWeight(weight);
            c.setNorm(norms.get(i));
            c.setValue(values.get(i));
            list.add(c);

            total = total + weight;
            price = price.add(prices.get(i));
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }

    private RestResult createReturnComms(TPurchaseOrder order, int rid, List<Integer> types, List<Integer> commoditys, List<BigDecimal> prices,
                                         List<Integer> weights, List<Integer> values, List<TPurchaseCommodity> list) {
        // 生成退货单
        int size = commoditys.size();
        if (size != types.size() || size != prices.size() || size != weights.size() || size != values.size()) {
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
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TPurchaseCommodity pc : purchaseCommodities) {
                if (pc.getCtype() == ctype && pc.getCid() == cid) {
                    find = true;
                    if (weight > pc.getWeight()) {
                        return RestResult.fail("退货商品重量不能大于采购重量:" + ctype + ", 商品id:" + cid);
                    }
                    if (value > pc.getValue()) {
                        return RestResult.fail("退货商品件数不能大于采购件数:" + ctype + ", 商品id:" + cid);
                    }

                    TPurchaseCommodity c = new TPurchaseCommodity();
                    c.setCtype(ctype);
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
                return RestResult.fail("未查询到商品id:" + cid + ", 类型:" + ctype);
            }
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }
}
