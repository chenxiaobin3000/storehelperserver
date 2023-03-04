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
import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction.*;

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
    private FinanceService financeService;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseCommodityRepository purchaseCommodityRepository;

    @Resource
    private PurchaseAttachmentRepository purchaseAttachmentRepository;

    @Resource
    private PurchaseReturnRepository purchaseReturnRepository;

    @Resource
    private PurchaseFareRepository purchaseFareRepository;

    @Resource
    private PurchaseRemarkRepository purchaseRemarkRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 采购进货
     */
    public RestResult purchase(int id, TPurchaseOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_purchase_apply, mp_purchase_purchase_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成采购单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createPurchaseComms(order, types, commoditys, values, prices, comms);
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
     * desc: 原料采购修改
     */
    public RestResult setPurchase(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
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
        ret = createPurchaseComms(order, types, commoditys, values, prices, comms);
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
        if (!purchaseCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!purchaseAttachmentRepository.deleteByOid(oid)) {
            return RestResult.fail("删除关联商品附件失败");
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

        // 财务记录
        if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_PAY, order.getId(), order.getPrice().negate())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = purchaseFareRepository.findByOid(oid);
        if (null != fares && !fares.isEmpty()) {
            for (TPurchaseFare fare : fares) {
                if (null == fare.getReview()) {
                    fare.setReview(id);
                    fare.setReviewTime(reviewTime);
                    if (!purchaseFareRepository.update(fare)) {
                        return RestResult.fail("更新运费信息失败");
                    }
                    if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_FARE, order.getId(), fare.getFare().negate())) {
                        return RestResult.fail("添加运费记录失败");
                    }
                }
            }
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokePurchase(int id, int oid) {
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), mp_purchase_purchase_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!purchaseOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 财务记录
        if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_PAY, order.getId(), order.getPrice())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = purchaseFareRepository.findByOid(oid);
        if (null != fares && !fares.isEmpty()) {
            for (TPurchaseFare fare : fares) {
                if (null != fare.getReview()) {
                    if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_FARE, order.getId(), fare.getFare())) {
                        return RestResult.fail("添加运费记录失败");
                    }
                }
            }
            if (!purchaseFareRepository.setReviewNull(oid)) {
                return RestResult.fail("更新运费信息失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult addPurchaseInfo(int id, int oid, BigDecimal fare, String remark) {
        // 验证公司
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能由申请人添加信息");
        }
        purchaseOrderService.clean(oid);

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!purchaseFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加采购物流费用失败");
            }
        }

        // 备注
        if (null != remark && remark.length() > 0) {
            if (!purchaseRemarkRepository.insert(oid, remark, new Date())) {
                return RestResult.fail("添加采购物流备注失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delPurchaseInfo(int id, int oid, int fid, int rid) {
        // 验证公司
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        purchaseOrderService.clean(oid);

        // 运费由申请人删，已审核由审核人删，备注由审核人删
        if (0 != fid) {
            TPurchaseFare fare = purchaseFareRepository.find(fid);
            if (null == fare) {
                return RestResult.fail("未查询到运费信息");
            }
            if (null != fare.getReview()) {
                if (!fare.getReview().equals(id)) {
                    return RestResult.fail("要删除已审核信息，请联系审核人");
                }
                if (!purchaseFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
            } else {
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人删除信息");
                }
                if (!purchaseFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
            }
        }

        // 备注由审核人删
        if (0 != rid) {
            if (!order.getReview().equals(rid)) {
                RestResult.fail("要删除备注，请联系订单审核人");
            }
            if (!purchaseRemarkRepository.delete(rid)) {
                return RestResult.fail("删除备注信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 原料退货
     */
    public RestResult returnc(int id, TPurchaseOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        // 采购单未审核，已入库都不能退货
        int rid = order.getRid();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(rid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到采购单");
        }
        if (null == purchaseOrder.getReview()) {
            return RestResult.fail("采购单未审核通过，不能进行入库");
        }
        if (purchaseReturnRepository.checkByPid(rid)) {
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
        ret = createReturnComms(order, rid, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        // 生成退货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!purchaseOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = purchaseOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!purchaseFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加采购物流费用失败");
            }
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 原料退货修改
     */
    public RestResult setReturn(int id, TPurchaseOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        // 已经审核的订单不能修改
        int oid = order.getId();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(oid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != purchaseOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        order.setGid(purchaseOrder.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_return_apply, mp_purchase_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createReturnComms(order, purchaseOrder.getRid(), types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        // 插入订单商品和附件数据
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        String msg = purchaseOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            purchaseFareRepository.delete(oid);
            if (!purchaseFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加退货物流费用失败");
            }
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
        if (!purchaseCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!purchaseAttachmentRepository.deleteByOid(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!purchaseOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        // TODO 删运费
        return reviewService.delete(review, order.getOtype(), oid);
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

        // TODO 校验所有退货单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 修改对应进货单数据
        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getRid());
        if (null == purchase) {
            return RestResult.fail("未查询到对应的进货单");
        }
        purchase.setCurUnit(purchase.getCurUnit() - order.getUnit());
        purchase.setCurPrice(purchase.getCurPrice().subtract(order.getPrice()));
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改进货单数据失败");
        }

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!purchaseReturnRepository.insert(order.getId(), order.getRid())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // 财务记录
        if (!financeService.insertRecord(id, group.getGid(), FINANCE_PURCHASE_RET, order.getId(), order.getPrice())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = purchaseFareRepository.findByOid(oid);
        for (TPurchaseFare fare : fares) {
            if (!financeService.insertRecord(id, group.getGid(), FINANCE_PURCHASE_FARE2, order.getId(), fare.getFare().negate())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), mp_purchase_return_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!purchaseOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!purchaseReturnRepository.delete(order.getId(), order.getRid())) {
            return RestResult.fail("删除采购退货信息失败");
        }

        // 财务记录
        BigDecimal money = purchaseCommodityRepository.count(oid);
        if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_RET, order.getId(), money.negate())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = purchaseFareRepository.findByOid(oid);
        for (TPurchaseFare fare : fares) {
            if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_FARE2, order.getId(), fare.getFare())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult addReturnInfo(int id, int oid, BigDecimal fare, String remark) {
        return addPurchaseInfo(id, oid, fare, remark);
    }

    public RestResult delReturnInfo(int id, int oid, int fid, int rid) {
        return delReturnInfo(id, oid, fid, rid);
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

    private RestResult createPurchaseComms(TPurchaseOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<TPurchaseCommodity> list) {
        // 生成采购单
        int size = commoditys.size();
        if (size != types.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            CommodityType type = CommodityType.valueOf(types.get(i));
            int cid = commoditys.get(i);
            int unit = 0;
            switch (type) {
                case ORIGINAL:
                    TOriginal original = originalRepository.find(cid);
                    if (null == original) {
                        return RestResult.fail("未查询到原料：" + cid);
                    }
                    unit = original.getUnit();
                    break;
                case STANDARD:
                    TStandard standard = standardRepository.find(cid);
                    if (null == standard) {
                        return RestResult.fail("未查询到标品：" + cid);
                    }
                    unit = standard.getUnit();
                    break;
                default:
                    return RestResult.fail("商品类型异常：" + type);
            }

            // 生成数据
            TPurchaseCommodity c = new TPurchaseCommodity();
            c.setCtype(type.getValue());
            c.setCid(cid);
            c.setUnit(unit);
            c.setValue(values.get(i));
            c.setPrice(prices.get(i));
            list.add(c);

            total = total + unit * values.get(i);
            price = price.add(prices.get(i));
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }

    private RestResult createReturnComms(TPurchaseOrder order, int rid, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<TPurchaseCommodity> list) {
        // 生成退货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        val purchaseCommodities = purchaseCommodityRepository.find(rid);
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            boolean find = false;
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            for (TPurchaseCommodity pc : purchaseCommodities) {
                if (pc.getCtype() == ctype && pc.getCid() == cid) {
                    find = true;
                    // 生成数据
                    TPurchaseCommodity c = new TPurchaseCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    c.setUnit(pc.getUnit());
                    c.setValue(values.get(i));
                    c.setPrice(prices.get(i));
                    list.add(c);

                    // 校验商品退货数不能大于采购单
                    if (values.get(i) > pc.getValue()) {
                        return RestResult.fail("退货商品数量不能大于采购数量, 商品id:" + cid + ", 类型:" + ctype);
                    }

                    total = total + pc.getUnit() * values.get(i);
                    price = price.add(prices.get(i));
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
