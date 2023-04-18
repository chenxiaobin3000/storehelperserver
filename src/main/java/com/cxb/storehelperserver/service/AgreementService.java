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
import java.math.RoundingMode;
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.COMMODITY;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.AGREEMENT_SHIPPED_ORDER;

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
    private StockService stockService;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private AgreementAttachmentRepository agreementAttachmentRepository;

    @Resource
    private AgreementReturnRepository agreementReturnRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 履约发货
     */
    public RestResult shipped(int id, TAgreementOrder order, List<Integer> commoditys, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_shipped_apply, mp_agreement_shipped_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成发货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createShippedComms(order, commoditys, weights, norms, values, comms);
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
    public RestResult setShipped(int id, int oid, int sid, int aid, Date applyTime, List<Integer> commoditys, List<Integer> weights,
                                 List<String> norms, List<Integer> values, List<Integer> attrs) {
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

        order.setAid(aid);
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
        ret = createShippedComms(order, commoditys, weights, norms, values, comms);
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

        // TODO 库存校验

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = stockService.handleAgreementStock(order, false);
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
        // 已销售或退货的不能撤销
        if (order.getValue() > order.getCurValue()) {
            return RestResult.fail("已销售或退货的订单不能撤销");
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
        msg = stockService.handleAgreementStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 履约退货
     */
    public RestResult returnc(int id, TAgreementOrder order, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 发货单未审核不能退货
        int rid = order.getRid();
        TAgreementOrder agreement = agreementOrderRepository.find(rid);
        if (null == agreement) {
            return RestResult.fail("未查询到履约单信息");
        }
        if (!agreement.getOtype().equals(AGREEMENT_SHIPPED_ORDER.getValue())) {
            return RestResult.fail("履约单据类型异常");
        }
        if (null == agreement.getReview()) {
            return RestResult.fail("履约单未审核通过，不能进行入库");
        }

        order.setGid(agreement.getGid());
        order.setSid(agreement.getSid());
        order.setAid(agreement.getAid());
        order.setAsid(agreement.getAsid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_return_apply, mp_agreement_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createReturnComms(order, order.getRid(), commoditys, weights, values, comms);
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
    public RestResult setReturn(int id, int oid, Date applyTime, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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
        ret = createReturnComms(order, order.getRid(), commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
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
        return delShipped(id, oid);
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
            return RestResult.fail("未查询到对应的履约单");
        }
        int value = agreement.getCurValue() - order.getValue();
        if (value < 0) {
            return RestResult.fail("退货商品总件数不能超出履约订单总件数");
        }
        BigDecimal price = agreement.getCurPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("退货商品总价不能超出履约订单总价");
        }
        if (0 == value) {
            agreement.setComplete(new Byte("1"));
        }
        agreement.setCurValue(value);
        agreement.setCurPrice(price);
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改履约单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        order.setComplete(new Byte("1"));
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!agreementReturnRepository.insert(oid, order.getRid())) {
            return RestResult.fail("添加履约退货信息失败");
        }

        // 增加库存
        String msg = stockService.handleAgreementStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
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

        // 还原扣除的履约单数量
        TAgreementOrder agreement = agreementOrderRepository.find(order.getRid());
        if (null == agreement) {
            return RestResult.fail("未查询到对应的履约单");
        }
        agreement.setCurValue(agreement.getCurValue() + order.getValue());
        agreement.setCurPrice(agreement.getCurPrice().add(order.getPrice()));
        agreement.setComplete(new Byte("0"));
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改履约单数据失败");
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
        msg = stockService.handleAgreementStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
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

    private RestResult createShippedComms(TAgreementOrder order, List<Integer> commoditys, List<Integer> weights, List<String> norms, List<Integer> values, List<TAgreementCommodity> list) {
        // 生成发货单
        int size = commoditys.size();
        if (size != weights.size() || size != norms.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int sid = order.getSid();
        int total = 0;
        int all = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            TStockDay stock = stockService.getStockCommodity(order.getGid(), sid, COMMODITY.getValue(), cid);
            if (null == stock) {
                return RestResult.fail("未查询到库存类型:" + cid);
            }
            if (weight > stock.getWeight()) {
                return RestResult.fail("库存商品重量不足:" + cid);
            }
            if (value > stock.getValue()) {
                return RestResult.fail("库存商品件数不足:" + cid);
            }

            TAgreementCommodity c = new TAgreementCommodity();
            c.setCid(cid);
            if (weight == stock.getWeight()) {
                c.setPrice(stock.getPrice());
            } else {
                c.setPrice(stock.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(stock.getWeight()), 2, RoundingMode.DOWN));
            }
            c.setWeight(weight);
            c.setNorm(norms.get(i));
            c.setValue(value);
            c.setCurValue(value);
            list.add(c);

            total = total + weight;
            all = all + value;
            price = price.add(c.getPrice());
        }
        order.setUnit(total);
        order.setValue(all);
        order.setPrice(price);
        order.setCurValue(all);
        order.setCurPrice(price);
        return null;
    }

    private RestResult createReturnComms(TAgreementOrder order, int aid, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TAgreementCommodity> list) {
        // 生成发货单
        int size = commoditys.size();
        if (size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val agreementCommodities = agreementCommodityRepository.find(aid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到履约商品信息");
        }
        int total = 0;
        int all = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TAgreementCommodity ac : agreementCommodities) {
                if (ac.getCid() == cid) {
                    find = true;
                    if (weight > ac.getWeight()) {
                        return RestResult.fail("退货商品重量不能大于发货重量:" + cid);
                    }
                    if (value > ac.getValue()) {
                        return RestResult.fail("退货商品件数不能大于发货件数:" + cid);
                    }

                    TAgreementCommodity c = new TAgreementCommodity();
                    c.setCid(cid);
                    if (weight == ac.getWeight()) {
                        c.setPrice(ac.getPrice());
                    } else {
                        c.setPrice(ac.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(ac.getWeight()), 2, RoundingMode.DOWN));
                    }
                    c.setWeight(weight);
                    c.setNorm(ac.getNorm());
                    c.setValue(value);
                    c.setCurValue(value);
                    list.add(c);

                    total = total + weight;
                    all = all + value;
                    price = price.add(c.getPrice());
                    break;
                }
            }
            if (!find) {
                return RestResult.fail("未查询到商品id:" + cid);
            }
        }
        order.setUnit(total);
        order.setValue(all);
        order.setPrice(price);
        order.setCurValue(all);
        order.setCurPrice(price);
        return null;
    }
}
