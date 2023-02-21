package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;
import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction.*;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.PURCHASE_IN_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.PURCHASE_OUT_ORDER;

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
    public RestResult purchase(int id, TPurchaseOrder order, List<Integer> types, List<Integer> commoditys,
                               List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_in_apply, mp_purchase_in_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成采购单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createPurchaseComms(types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        // 生成采购单批号
        String batch = dateUtil.createBatch(String.valueOf(PURCHASE_IN_ORDER.getValue()));
        order.setBatch(batch);
        if (!purchaseOrderRepository.insert(order)) {
            return RestResult.fail("生成采购订单失败");
        }
        int oid = order.getId();
        String msg = purchaseOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        return reviewService.apply(id, order.getGid(), order.getSid(), PURCHASE_IN_ORDER.getValue(), oid, batch, reviews);
    }

    /**
     * desc: 原料采购修改
     */
    public RestResult setPurchase(int id, TPurchaseOrder order, List<Integer> types, List<Integer> commoditys,
                                  List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_in_apply, mp_purchase_in_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(order.getId());
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != purchaseOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!purchaseOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(PURCHASE_IN_ORDER.getValue(), order.getId(), order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成采购单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createPurchaseComms(types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("生成采购订单失败");
        }
        String msg = purchaseOrderService.update(order.getId(), comms, attrs);
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
        if (!purchaseAttachmentRepository.delete(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!purchaseOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, PURCHASE_IN_ORDER.getValue(), oid);
    }

    public RestResult reviewPurchase(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        if (!reviewService.checkReview(id, PURCHASE_IN_ORDER.getValue(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 添加审核信息
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 财务记录
        BigDecimal money = purchaseCommodityRepository.count(oid);
        if (!financeService.insertRecord(id, group.getGid(), FINANCE_PURCHASE_PAY, order.getId(), money.negate())) {
            return RestResult.fail("添加财务记录失败");
        }
        if (!financeService.insertRecord(id, group.getGid(), FINANCE_PURCHASE_FARE, order.getId(), order.getFare().negate())) {
            return RestResult.fail("添加运费记录失败");
        }
        return reviewService.review(id, order.getGid(), order.getSid(),
                PURCHASE_IN_ORDER.getValue(), oid, order.getBatch(), order.getApplyTime());
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), PURCHASE_IN_ORDER.getValue(), oid, order.getBatch(), mp_purchase_in_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!purchaseOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 财务记录
        BigDecimal money = purchaseCommodityRepository.count(oid);
        if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_RET, order.getId(), money)) {
            return RestResult.fail("添加财务记录失败");
        }
        if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_FARE2, order.getId(), order.getFare())) {
            return RestResult.fail("添加运费记录失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 原料退货
     */
    public RestResult returnc(int id, TPurchaseOrder order, List<Integer> types, List<Integer> commoditys,
                              List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_out_apply, mp_purchase_out_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 采购单未审核，已入库都不能删除
        int rid = order.getRid();
        TPurchaseOrder purchase = purchaseOrderRepository.find(rid);
        if (null == purchase) {
            return RestResult.fail("未查询到采购单");
        }
        if (null == purchase.getReview()) {
            return RestResult.fail("未审批的采购单请直接删除");
        }
        if (purchaseReturnRepository.checkByPid(rid)) {
            return RestResult.fail("采购商品已入库，请使用仓储退货单");
        }

        // 生成退货单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createPurchaseComms(types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成采购单批号
        String batch = dateUtil.createBatch(String.valueOf(PURCHASE_OUT_ORDER.getValue()));
        order.setBatch(batch);
        if (!purchaseOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }

        // TODO 校验采购单商品数不能大于退货数

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = purchaseOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        return reviewService.apply(id, order.getGid(), order.getSid(), PURCHASE_OUT_ORDER.getValue(), oid, batch, reviews);
    }

    /**
     * desc: 原料退货修改
     */
    public RestResult setReturn(int id, TPurchaseOrder order, List<Integer> types, List<Integer> commoditys,
                                List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_purchase_out_apply, mp_purchase_out_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(order.getId());
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != purchaseOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!purchaseOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(PURCHASE_OUT_ORDER.getValue(), order.getId(), order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成采购单
        val comms = new ArrayList<TPurchaseCommodity>();
        ret = createPurchaseComms(types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("生成采购订单失败");
        }

        // TODO 校验采购单商品数不能大于退货数

        // 插入订单商品和附件数据
        String msg = purchaseOrderService.update(order.getId(), comms, attrs);
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
        if (!purchaseCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!purchaseAttachmentRepository.delete(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!purchaseOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, PURCHASE_OUT_ORDER.getValue(), oid);
    }

    public RestResult reviewReturn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        if (!reviewService.checkReview(id, PURCHASE_OUT_ORDER.getValue(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 添加审核信息
        TPurchaseOrder order = purchaseOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!purchaseOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        if (!purchaseReturnRepository.insert(order.getId(), order.getRid())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // 财务记录
        BigDecimal money = purchaseCommodityRepository.count(oid);
        if (!financeService.insertRecord(id, group.getGid(), FINANCE_PURCHASE_RET, order.getId(), money)) {
            return RestResult.fail("添加财务记录失败");
        }
        if (!financeService.insertRecord(id, group.getGid(), FINANCE_PURCHASE_FARE2, order.getId(), order.getFare())) {
            return RestResult.fail("添加运费记录失败");
        }
        return reviewService.review(id, order.getGid(), order.getSid(),
                PURCHASE_OUT_ORDER.getValue(), oid, order.getBatch(), order.getApplyTime());
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), PURCHASE_OUT_ORDER.getValue(), oid, order.getBatch(), mp_purchase_out_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!purchaseOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // TODO 删除
        if (!purchaseReturnRepository.delete(order.getRid(), order.getId())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // 财务记录
        BigDecimal money = purchaseCommodityRepository.count(oid);
        if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_RET, order.getId(), money.negate())) {
            return RestResult.fail("添加财务记录失败");
        }
        if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_FARE2, order.getId(), order.getFare().negate())) {
            return RestResult.fail("添加运费记录失败");
        }
        return RestResult.ok();
    }

    public List<MyOrderCommodity> getOrderCommodity(int id, int gid, int oid) {
        return null;
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

    private RestResult createPurchaseComms(List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<TPurchaseCommodity> list) {
        // 生成采购单
        int size = commoditys.size();
        if (size != types.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
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
        }
        return null;
    }

    private RestResult createPurchaseComms(List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TPurchaseCommodity> list) {
        // 生成采购单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息出错");
        }
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            CommodityType type = CommodityType.valueOf(types.get(i));
            int cid = commoditys.get(i);
            switch (type) {
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
                    return RestResult.fail("商品类型异常：" + type);
            }

            // 生成数据
            TPurchaseCommodity c = new TPurchaseCommodity();
            c.setCtype(type.getValue());
            c.setCid(cid);
            c.setUnit(0);
            c.setValue(values.get(i));
            c.setPrice(new BigDecimal(0));
            list.add(c);
        }
        return null;
    }
}
