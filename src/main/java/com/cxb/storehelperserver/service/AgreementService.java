package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.TypeDefine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction.*;
import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction.FINANCE_PURCHASE_FARE2;

/**
 * desc: 履约业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AgreementService {
    @Resource
    private CheckService checkService;

    @Resource
    private AgreementOrderService agreementOrderService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private FinanceService financeService;

    @Resource
    private StorageStockService storageStockService;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private AgreementAttachmentRepository agreementAttachmentRepository;

    @Resource
    private AgreementFareRepository agreementFareRepository;

    @Resource
    private AgreementRemarkRepository agreementRemarkRepository;

    @Resource
    private AgreementReturnRepository agreementReturnRepository;

    @Resource
    private CloudAgreementRepository cloudAgreementRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private StockRepository stockRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 履约发货
     */
    public RestResult shipped(int id, TAgreementOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_shipped_apply, mp_agreement_shipped_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成发货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createShippedComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成发货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!agreementOrderRepository.insert(order)) {
            return RestResult.fail("生成发货订单失败");
        }
        int oid = order.getId();
        String msg = agreementOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 履约发货修改
     */
    public RestResult setShipped(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        RestResult ret = check(id, order, mp_agreement_shipped_apply, mp_agreement_shipped_review, reviews);
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

        // 生成发货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createShippedComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("生成发货订单失败");
        }
        String msg = agreementOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delShipped(int id, int oid) {
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        agreementAttachmentRepository.deleteByOid(oid);
        if (!agreementCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!agreementOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewShipped(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 校验审核人员信息
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = storageStockService.handleAgreementStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeShipped(int id, int oid) {
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, agreement_shipped)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_agreement_shipped_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!agreementOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = storageStockService.handleAgreementStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addShippedInfo(int id, int oid, BigDecimal fare, String remark) {
        // 验证公司
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        agreementOrderService.clean(oid);

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!agreementFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加物流费用失败");
            }
        }

        // 备注
        if (null != remark && remark.length() > 0) {
            if (!agreementRemarkRepository.insert(oid, remark, new Date())) {
                return RestResult.fail("添加备注失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delShippedInfo(int id, int oid, int fid, int rid) {
        // 验证公司
        TAgreementOrder order = agreementOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        agreementOrderService.clean(oid);

        // 运费由申请人删，已审核由审核人删，备注由审核人删
        if (0 != fid) {
            TAgreementFare fare = agreementFareRepository.find(fid);
            if (null == fare) {
                return RestResult.fail("未查询到运费信息");
            }
            if (null != fare.getReview()) {
                if (!fare.getReview().equals(id)) {
                    return RestResult.fail("要删除已审核信息，请联系审核人");
                }
                if (!agreementFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
            } else {
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人删除信息");
                }
                if (!agreementFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
            }
        }

        // 备注由审核人删
        if (0 != rid) {
            if (!order.getReview().equals(rid)) {
                RestResult.fail("要删除备注，请联系订单审核人");
            }
            if (!agreementRemarkRepository.delete(rid)) {
                return RestResult.fail("删除备注信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 履约退货
     */
    public RestResult returnc(int id, TAgreementOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        // 发货单未审核，已入库都不能退货
        int rid = order.getRid();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(rid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到履约单");
        }
        if (null == purchaseOrder.getReview()) {
            return RestResult.fail("履约单未审核通过，不能进行入库");
        }
        if (cloudAgreementRepository.checkByAid(rid)) {
            return RestResult.fail("履约商品已入库，请使用云仓退货单");
        }

        order.setGid(purchaseOrder.getGid());
        order.setSid(purchaseOrder.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_return_apply, mp_agreement_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createReturnComms(order, order.getRid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成退货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!agreementOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        int oid = order.getId();
        String msg = agreementOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 履约退货修改
     */
    public RestResult setReturn(int id, int oid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        RestResult ret = check(id, order, mp_agreement_return_apply, mp_agreement_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createReturnComms(order, order.getRid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 插入订单商品和附件数据
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        String msg = agreementOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delReturn(int id, int oid) {
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        agreementAttachmentRepository.deleteByOid(oid);
        if (!agreementCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!agreementOrderRepository.delete(oid)) {
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
        TAgreementOrder order = agreementOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出采购单
        TAgreementOrder agreement = agreementOrderRepository.find(order.getRid());
        if (null == agreement) {
            return RestResult.fail("未查询到对应的发货单");
        }
        int unit = agreement.getCurUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("退货商品总量不能超出发货订单总量");
        }
        BigDecimal price = agreement.getCurPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("退货商品总价不能超出发货订单总价");
        }
        agreement.setCurUnit(unit);
        agreement.setCurPrice(price);
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改发货单数据失败");
        }

        // TODO 采购数量为0时，标记采购完成

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!agreementReturnRepository.insert(oid, order.getRid())) {
            return RestResult.fail("添加履约退货信息失败");
        }

        // 增加库存
        String msg = storageStockService.handleAgreementStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, agreement_return)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_agreement_return_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!agreementOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!agreementReturnRepository.delete(oid, order.getRid())) {
            return RestResult.fail("删除采购退货信息失败");
        }

        // 减少库存
        msg = storageStockService.handleAgreementStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addReturnInfo(int id, int oid, BigDecimal fare, String remark) {
        return addShippedInfo(id, oid, fare, remark);
    }

    public RestResult delReturnInfo(int id, int oid, int fid, int rid) {
        return delShippedInfo(id, oid, fid, rid);
    }

    private RestResult check(int id, TAgreementOrder order, int applyPerm, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(id, gid, applyPerm, reviewPerm, reviews);
    }

    private RestResult createShippedComms(TAgreementOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TAgreementCommodity> list) {
        // 生成发货单
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
            TStock stock = stockRepository.find(sid, ctype, cid);
            if (null == stock) {
                return RestResult.fail("未查询到库存类型:" + types.get(i) + ",商品:" + commoditys.get(i));
            }
            if (stock.getValue() < value) {
                return RestResult.fail("库存商品数量不足:" + types.get(i) + ",商品:" + commoditys.get(i));
            }

            // 生成数据
            TAgreementCommodity c = new TAgreementCommodity();
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

    private RestResult createReturnComms(TAgreementOrder order, int aid, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TAgreementCommodity> list) {
        // 生成发货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val agreementCommodities = agreementCommodityRepository.find(aid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到履约商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int value = values.get(i);
            boolean find = false;
            for (TAgreementCommodity ac : agreementCommodities) {
                if (ac.getCtype() == ctype && ac.getCid() == cid) {
                    find = true;

                    // 生成数据
                    TAgreementCommodity c = new TAgreementCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    c.setValue(value);
                    c.setPrice(ac.getPrice());
                    list.add(c);

                    // 校验商品退货数不能大于发货单
                    if (value > ac.getValue()) {
                        return RestResult.fail("退货商品数量不能大于发货数量, 商品id:" + cid + ", 类型:" + ctype);
                    }

                    total = total + value;
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
}
