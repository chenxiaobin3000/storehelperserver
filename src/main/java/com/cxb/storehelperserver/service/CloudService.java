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
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction.*;

/**
 * desc: 损耗业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CloudService {
    @Resource
    private CheckService checkService;

    @Resource
    private CloudOrderService cloudOrderService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private FinanceService financeService;

    @Resource
    private CloudStockService cloudStockService;

    @Resource
    private CloudOrderRepository cloudOrderRepository;

    @Resource
    private CloudCommodityRepository cloudCommodityRepository;

    @Resource
    private CloudAttachmentRepository cloudAttachmentRepository;

    @Resource
    private CloudPurchaseRepository cloudPurchaseRepository;

    @Resource
    private CloudFareRepository cloudFareRepository;

    @Resource
    private CloudRemarkRepository cloudRemarkRepository;

    @Resource
    private CloudReturnRepository cloudReturnRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseCommodityRepository purchaseCommodityRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private CloudStockRepository cloudStockRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 云仓采购入库
     */
    public RestResult purchase(int id, TCloudOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        // 采购单未审核不能入库
        int rid = order.getOid();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(rid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到采购单");
        }
        if (null == purchaseOrder.getReview()) {
            return RestResult.fail("采购单未审核通过，不能进行入库");
        }

        order.setGid(purchaseOrder.getGid());
        order.setSid(purchaseOrder.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_purchase_apply, mp_cloud_purchase_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createPurchaseComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!cloudOrderRepository.insert(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        int oid = order.getId();
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 云仓采购入库修改
     */
    public RestResult setPurchase(int id, int oid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TCloudOrder order = cloudOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_purchase_apply, mp_cloud_purchase_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createPurchaseComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delPurchase(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        cloudAttachmentRepository.deleteByOid(oid);
        if (!cloudCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!cloudOrderRepository.delete(oid)) {
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
        TCloudOrder order = cloudOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验入库总量不能超出采购单
        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getOid());
        if (null == purchase) {
            return RestResult.fail("未查询到对应的采购单");
        }
        int unit = purchase.getCurUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("入库商品总量不能超出发货订单总量");
        }
        purchase.setCurUnit(unit);
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改进货单数据失败");
        }
        // TODO 修改采购单完成状态

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!cloudPurchaseRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加履约入库信息失败");
        }

        // 增加库存
        String msg = cloudStockService.handlePurchaseStock(order, purchase, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokePurchase(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, cloud_purchase)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getOid());
        if (null == purchase) {
            return RestResult.fail("未查询到对应的采购单");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_cloud_purchase_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!cloudOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!cloudPurchaseRepository.delete(oid, order.getOid())) {
            return RestResult.fail("撤销履约入库信息失败");
        }

        // 减少库存
        msg = cloudStockService.handlePurchaseStock(order, purchase, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addPurchaseInfo(int id, int oid, BigDecimal fare, String remark) {
        // 验证公司
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        cloudOrderService.clean(oid);

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!cloudFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加物流费用失败");
            }
        }

        // 备注
        if (null != remark && remark.length() > 0) {
            if (!cloudRemarkRepository.insert(oid, remark, new Date())) {
                return RestResult.fail("添加备注失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delPurchaseInfo(int id, int oid, int fid, int rid) {
        // 验证公司
        TCloudOrder order = cloudOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        cloudOrderService.clean(oid);

        // 运费由申请人删，已审核由审核人删，备注由审核人删
        if (0 != fid) {
            TCloudFare fare = cloudFareRepository.find(fid);
            if (null == fare) {
                return RestResult.fail("未查询到运费信息");
            }
            if (null != fare.getReview()) {
                if (!fare.getReview().equals(id)) {
                    return RestResult.fail("要删除已审核信息，请联系审核人");
                }
                if (!cloudFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
            } else {
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人删除信息");
                }
                if (!cloudFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
            }
        }

        // 备注由审核人删
        if (0 != rid) {
            if (!order.getReview().equals(rid)) {
                RestResult.fail("要删除备注，请联系订单审核人");
            }
            if (!cloudRemarkRepository.delete(rid)) {
                return RestResult.fail("删除备注信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 云仓履约入库
     */
    public RestResult agreement(int id, TCloudOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        // 发货单未审核不能入库
        int rid = order.getOid();
        TAgreementOrder agreementOrder = agreementOrderRepository.find(rid);
        if (null == agreementOrder) {
            return RestResult.fail("未查询到发货单");
        }
        if (null == agreementOrder.getReview()) {
            return RestResult.fail("发货单未审核通过，不能进行入库");
        }

        order.setGid(agreementOrder.getGid());
        order.setSid(agreementOrder.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_agreement_apply, mp_cloud_agreement_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createAgreementComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!cloudOrderRepository.insert(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        int oid = order.getId();
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 云仓履约入库修改
     */
    public RestResult setAgreement(int id, int oid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TCloudOrder order = cloudOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_agreement_apply, mp_cloud_agreement_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createAgreementComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delAgreement(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        cloudAttachmentRepository.deleteByOid(oid);
        if (!cloudCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!cloudOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewAgreement(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TCloudOrder order = cloudOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验入库总量不能超出采购单
        TAgreementOrder agreement = agreementOrderRepository.find(order.getOid());
        if (null == agreement) {
            return RestResult.fail("未查询到对应的发货单");
        }
        int unit = agreement.getCurUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("入库商品总量不能超出发货订单总量");
        }
        agreement.setCurUnit(unit);
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改进货单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!cloudPurchaseRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加履约入库信息失败");
        }

        // 增加库存
        String msg = cloudStockService.handleAgreementStock(order, agreement, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeAgreement(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, cloud_agreement)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        TAgreementOrder agreement = agreementOrderRepository.find(order.getOid());
        if (null == agreement) {
            return RestResult.fail("未查询到对应的发货单");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_cloud_agreement_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!cloudOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!cloudPurchaseRepository.delete(oid, order.getOid())) {
            return RestResult.fail("撤销履约入库信息失败");
        }

        // 减少库存
        msg = cloudStockService.handleAgreementStock(order, agreement, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addAgreementInfo(int id, int oid, BigDecimal fare, String remark) {
        return addPurchaseInfo(id, oid, fare, remark);
    }

    public RestResult delAgreementInfo(int id, int oid, int fid, int rid) {
        return delPurchaseInfo(id, oid, fid, rid);
    }

    /**
     * desc: 云仓损耗
     */
    public RestResult loss(int id, TCloudOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_loss_apply, mp_cloud_loss_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createLossComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!cloudOrderRepository.insert(order)) {
            return RestResult.fail("生成损耗订单失败");
        }
        int oid = order.getId();
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 云仓损耗修改
     */
    public RestResult setLoss(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TCloudOrder order = cloudOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_loss_apply, mp_cloud_loss_review, reviews);
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

        // 生成损耗单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createLossComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("生成损耗订单失败");
        }
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delLoss(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        cloudAttachmentRepository.deleteByOid(oid);
        if (!cloudCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!cloudOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewLoss(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 校验审核人员信息
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = cloudStockService.handleLossStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeLoss(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, cloud_loss)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_cloud_loss_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!cloudOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = cloudStockService.handleLossStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addLossInfo(int id, int oid, String remark) {
        // 验证公司
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        cloudOrderService.clean(oid);

        // 备注
        if (null != remark && remark.length() > 0) {
            if (!cloudRemarkRepository.insert(oid, remark, new Date())) {
                return RestResult.fail("添加备注失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delLossInfo(int id, int oid, int rid) {
        // 验证公司
        TCloudOrder order = cloudOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        cloudOrderService.clean(oid);

        // 备注由审核人删
        if (0 != rid) {
            if (!order.getReview().equals(rid)) {
                RestResult.fail("要删除备注，请联系订单审核人");
            }
            if (!cloudRemarkRepository.delete(rid)) {
                return RestResult.fail("删除备注信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 云仓退货到仓库
     */
    public RestResult returnc(int id, TCloudOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        // 入库单未审核不能退货
        int rid = order.getOid();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(rid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到采购单");
        }
        if (null == purchaseOrder.getReview()) {
            return RestResult.fail("入库单未审核通过，不能进行退货");
        }
        if (!cloudPurchaseRepository.checkByPid(rid)) {
            return RestResult.fail("采购商品未入库，请使用采购退货单");
        }

        order.setGid(purchaseOrder.getGid());
        order.setSid(purchaseOrder.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_return_apply, mp_cloud_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createReturnComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成退货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!cloudOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!cloudFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加退货物流费用失败");
            }
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 云仓退货修改
     */
    public RestResult setReturn(int id, TCloudOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        // 已经审核的订单不能修改
        int oid = order.getId();
        TCloudOrder cloudOrder = cloudOrderRepository.find(oid);
        if (null == cloudOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != cloudOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        order.setGid(cloudOrder.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_return_apply, mp_cloud_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createReturnComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 插入订单商品和附件数据
        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            cloudFareRepository.delete(oid);
            if (!cloudFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加退货物流费用失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delReturn(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        if (!cloudCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!cloudAttachmentRepository.deleteByOid(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!cloudOrderRepository.delete(oid)) {
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

        // 校验审核人员信息
        TCloudOrder order = cloudOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验所有退货单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 修改对应进货单数据
        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getOid());
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
        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!cloudReturnRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // TODO 减少库存

        // 财务记录
        if (!financeService.insertRecord(id, group.getGid(), FINANCE_STORAGE_RET, order.getId(), order.getPrice())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = cloudFareRepository.findByOid(oid);
        for (TCloudFare fare : fares) {
            if (!financeService.insertRecord(id, group.getGid(), FINANCE_STORAGE_FARE2, order.getId(), fare.getFare().negate())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, cloud_return)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_cloud_return_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!cloudOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!cloudReturnRepository.delete(oid)) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // TODO 增加库存

        // 财务记录
        BigDecimal money = purchaseCommodityRepository.count(oid);
        if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_RET, order.getId(), money.negate())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = cloudFareRepository.findByOid(oid);
        for (TCloudFare fare : fares) {
            if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_FARE2, order.getId(), fare.getFare())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 云仓退货到采购
     */
    public RestResult backc(int id, TCloudOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        // 入库单未审核不能退货
        int rid = order.getOid();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(rid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到采购单");
        }
        if (null == purchaseOrder.getReview()) {
            return RestResult.fail("入库单未审核通过，不能进行退货");
        }

        order.setGid(purchaseOrder.getGid());
        order.setSid(purchaseOrder.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_return_apply, mp_cloud_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createReturnComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成退货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!cloudOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!cloudFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加退货物流费用失败");
            }
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 云仓退货修改
     */
    public RestResult setBack(int id, TCloudOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        // 已经审核的订单不能修改
        int oid = order.getId();
        TCloudOrder cloudOrder = cloudOrderRepository.find(oid);
        if (null == cloudOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != cloudOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        order.setGid(cloudOrder.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_cloud_return_apply, mp_cloud_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TCloudCommodity>();
        ret = createReturnComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 插入订单商品和附件数据
        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        String msg = cloudOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            cloudFareRepository.delete(oid);
            if (!cloudFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加退货物流费用失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delBack(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        if (!cloudCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!cloudAttachmentRepository.deleteByOid(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!cloudOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewBack(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TCloudOrder order = cloudOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验所有退货单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 修改对应进货单数据
        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getOid());
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
        if (!cloudOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!cloudReturnRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // TODO 减少库存

        // 财务记录
        if (!financeService.insertRecord(id, group.getGid(), FINANCE_STORAGE_RET, order.getId(), order.getPrice())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = cloudFareRepository.findByOid(oid);
        for (TCloudFare fare : fares) {
            if (!financeService.insertRecord(id, group.getGid(), FINANCE_STORAGE_FARE2, order.getId(), fare.getFare().negate())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeBack(int id, int oid) {
        TCloudOrder order = cloudOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, cloud_return)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_cloud_return_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!cloudOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!cloudReturnRepository.delete(oid)) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // TODO 增加库存

        // 财务记录
        BigDecimal money = purchaseCommodityRepository.count(oid);
        if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_RET, order.getId(), money.negate())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = cloudFareRepository.findByOid(oid);
        for (TCloudFare fare : fares) {
            if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_FARE2, order.getId(), fare.getFare())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TCloudOrder order, int applyPerm, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(id, gid, applyPerm, reviewPerm, reviews);
    }

    private RestResult createPurchaseComms(TCloudOrder order, int pid, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TCloudCommodity> list) {
        // 生成入库单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val purchaseCommodities = purchaseCommodityRepository.find(pid);
        if (null == purchaseCommodities || purchaseCommodities.isEmpty()) {
            return RestResult.fail("未查询到采购商品信息");
        }
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
                    int value = values.get(i);
                    TCloudCommodity c = new TCloudCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    c.setValue(value);
                    c.setPrice(pc.getPrice());
                    list.add(c);

                    // 校验商品入库数不能大于采购单
                    if (value > pc.getValue()) {
                        return RestResult.fail("入库商品数量不能大于采购数量, 商品id:" + cid + ", 类型:" + ctype);
                    }

                    total = total + pc.getUnit() * value;
                    price = price.add(pc.getPrice().multiply(new BigDecimal(value)));
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

    private RestResult createAgreementComms(TCloudOrder order, int pid, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TCloudCommodity> list) {
        // 生成入库单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val agreementCommodities = agreementCommodityRepository.find(pid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到发货商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            boolean find = false;
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            for (TAgreementCommodity ac : agreementCommodities) {
                if (ac.getCtype() == ctype && ac.getCid() == cid) {
                    find = true;
                    // 生成数据
                    int value = values.get(i);
                    TCloudCommodity c = new TCloudCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    c.setValue(value);
                    c.setPrice(ac.getPrice());
                    list.add(c);

                    // 校验商品入库数不能大于发货单
                    if (value > ac.getValue()) {
                        return RestResult.fail("入库商品数量不能大于发货数量, 商品id:" + cid + ", 类型:" + ctype);
                    }

                    total = total + ac.getUnit() * value;
                    price = price.add(ac.getPrice().multiply(new BigDecimal(value)));
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

    private RestResult createLossComms(TCloudOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TCloudCommodity> list) {
        // 生成损耗单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int sid = order.getSid();
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int value = values.get(i);
            TCloudStock stock = cloudStockRepository.find(sid, ctype, cid);
            if (null == stock) {
                return RestResult.fail("未查询到库存类型:" + types.get(i) + ",商品:" + commoditys.get(i));
            }
            if (stock.getValue() < value) {
                return RestResult.fail("库存商品数量不足:" + types.get(i) + ",商品:" + commoditys.get(i));
            }

            // 生成数据
            TCloudCommodity c = new TCloudCommodity();
            c.setCtype(ctype);
            c.setCid(cid);
            c.setValue(value);
            c.setPrice(stock.getPrice());
            list.add(c);

            total = total + value;
            price = price.add(stock.getPrice().multiply(new BigDecimal(value)));
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }

    private RestResult createReturnComms(TCloudOrder order, int rid, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TCloudCommodity> list) {
        // 生成退货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
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
                    TCloudCommodity c = new TCloudCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    c.setValue(values.get(i));
                    list.add(c);

                    // 校验商品退货数不能大于采购单
                    if (values.get(i) > pc.getValue()) {
                        return RestResult.fail("退货商品数量不能大于采购数量, 商品id:" + cid + ", 类型:" + ctype);
                    }

                    total = total + pc.getUnit() * values.get(i);
                    price = price.add(pc.getPrice());
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
