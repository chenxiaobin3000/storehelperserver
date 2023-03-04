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
    private AgreementReturnRepository agreementReturnRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 履约发货
     */
    public RestResult shipped(int id, TAgreementOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys,
                              List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_shipped_apply, mp_agreement_shipped_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成发货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createAgreementComms(order, types, commoditys, values, prices, comms);
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

        // TODO 扣库存
        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!agreementFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加发货物流费用失败");
            }
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 履约发货修改
     */
    public RestResult setShipped(int id, TAgreementOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys,
                                 List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_shipped_apply, mp_agreement_shipped_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        int oid = order.getId();
        TAgreementOrder agreementOrder = agreementOrderRepository.find(oid);
        if (null == agreementOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != agreementOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!agreementOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(order.getOtype(), oid, order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成发货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createAgreementComms(order, types, commoditys, values, prices, comms);
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

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            agreementFareRepository.delete(oid);
            if (!agreementFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加发货物流费用失败");
            }
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
        if (!agreementCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!agreementAttachmentRepository.deleteByOid(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!agreementOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        // TODO 删运费
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewShipped(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TAgreementOrder order = agreementOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验所有入库单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 财务记录
        val fares = agreementFareRepository.findByOid(oid);
        for (TAgreementFare fare : fares) {
            if (!financeService.insertRecord(id, group.getGid(), FINANCE_AGREEMENT_FARE, order.getId(), fare.getFare().negate())) {
                return RestResult.fail("添加运费记录失败");
            }
        }

        // 减少库存
        String msg = storageStockService.addStock(id, false, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid, order.getRid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeShipped(int id, int oid) {
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, agreement_shipped)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), mp_agreement_shipped_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!agreementOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 财务记录
        val fares = agreementFareRepository.findByOid(oid);
        for (TAgreementFare fare : fares) {
            if (!financeService.insertRecord(id, gid, FINANCE_AGREEMENT_FARE, order.getId(), fare.getFare())) {
                return RestResult.fail("添加运费记录失败");
            }
        }

        // 减少库存
        msg = storageStockService.addStock(id, true, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid, order.getRid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 履约退货
     */
    public RestResult returnc(int id, TAgreementOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys,
                              List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        // 发货单未审核，已入库都不能退货
        int rid = order.getRid();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(rid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到履约单");
        }
        if (null == purchaseOrder.getReview()) {
            return RestResult.fail("履约单未审核通过，不能进行入库");
        }
        if (agreementReturnRepository.checkByAid(rid)) {
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
        ret = createAgreementComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        // 生成退货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!agreementOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = agreementOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!agreementFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加退货物流费用失败");
            }
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 履约退货修改
     */
    public RestResult setReturn(int id, TAgreementOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys,
                                List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        // 已经审核的订单不能修改
        int oid = order.getId();
        TAgreementOrder agreementOrder = agreementOrderRepository.find(oid);
        if (null == agreementOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != agreementOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        order.setGid(agreementOrder.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_return_apply, mp_agreement_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createAgreementComms(order, types, commoditys, values, prices, comms);
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

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            agreementFareRepository.delete(oid);
            if (!agreementFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加退货物流费用失败");
            }
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
        if (!agreementCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!agreementAttachmentRepository.deleteByOid(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!agreementOrderRepository.delete(oid)) {
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
        TAgreementOrder order = agreementOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验所有退货单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 修改对应进货单数据
        TAgreementOrder agreement = agreementOrderRepository.find(order.getRid());
        if (null == agreement) {
            return RestResult.fail("未查询到对应的履约单");
        }
        agreement.setCurUnit(agreement.getCurUnit() - order.getUnit());
        agreement.setCurPrice(agreement.getCurPrice().subtract(order.getPrice()));
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改履约单数据失败");
        }

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!agreementReturnRepository.insert(order.getId(), order.getRid())) {
            return RestResult.fail("添加履约退货信息失败");
        }

        // TODO 增加库存

        // 财务记录
        val fares = agreementFareRepository.findByOid(oid);
        for (TAgreementFare fare : fares) {
            if (!financeService.insertRecord(id, group.getGid(), FINANCE_AGREEMENT_FARE2, order.getId(), fare.getFare().negate())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TAgreementOrder order = agreementOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, agreement_return)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), mp_agreement_return_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!agreementOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!agreementReturnRepository.delete(order.getId(), order.getRid())) {
            return RestResult.fail("删除采购退货信息失败");
        }

        // TODO 减少库存

        // 财务记录
        val fares = agreementFareRepository.findByOid(oid);
        for (TAgreementFare fare : fares) {
            if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_FARE2, order.getId(), fare.getFare())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return RestResult.ok();
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

    private RestResult createAgreementComms(TAgreementOrder order, List<Integer> types, List<Integer> commoditys,
                                            List<Integer> values, List<BigDecimal> prices, List<TAgreementCommodity> list) {
        // 生成进货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            TypeDefine.CommodityType type = TypeDefine.CommodityType.valueOf(types.get(i));
            int cid = commoditys.get(i);
            int unit = 0;
            switch (type) {
                case COMMODITY:
                    TCommodity find1 = commodityRepository.find(cid);
                    if (null == find1) {
                        return RestResult.fail("未查询到商品：" + cid);
                    }
                    unit = find1.getUnit();
                    break;
                case STANDARD:
                    TStandard find4 = standardRepository.find(cid);
                    if (null == find4) {
                        return RestResult.fail("未查询到标品：" + cid);
                    }
                    unit = find4.getUnit();
                    break;
                default:
                    return RestResult.fail("商品类型异常：" + type);
            }

            // TODO 校验商品退货数不能大于履约单

            // 生成数据
            TAgreementCommodity c = new TAgreementCommodity();
            c.setCtype(type.getValue());
            c.setCid(cid);
            c.setUnit(unit);
            c.setValue(values.get(i));
            c.setPrice(prices.get(i));
            list.add(c);
            total = total + unit * values.get(i);
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }
}
