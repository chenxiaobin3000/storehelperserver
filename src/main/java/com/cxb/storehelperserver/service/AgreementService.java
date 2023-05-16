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
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.AGREEMENT_SHIPPED_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.STORAGE_AGREEMENT_IN_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.STORAGE_AGREEMENT_OUT_ORDER;

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
    private StorageService storageService;

    @Resource
    private StockCloudService stockCloudService;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private AgreementAttachmentRepository agreementAttachmentRepository;

    @Resource
    private AgreementReturnRepository agreementReturnRepository;

    @Resource
    private AgreementStorageRepository agreementStorageRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 履约发货
     */
    public RestResult shipped(int id, TAgreementOrder order, int sid, int review, int storage, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_shipped, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成发货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createShippedComms(order, commoditys, prices, weights, norms, values, comms);
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

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            RestResult ret2 = reviewShipped(id, oid);
            if (RestResult.isOk(ret2) && storage > 0) {
                // 一键出库
                if (sid <= 0) {
                    return RestResult.fail("未指定仓库，一键出库失败");
                }
                TStorageOrder storageOrder = new TStorageOrder();
                storageOrder.setOtype(STORAGE_AGREEMENT_OUT_ORDER.getValue());
                storageOrder.setSid(sid);
                storageOrder.setTid(0);
                storageOrder.setApply(order.getApply());
                storageOrder.setApplyTime(order.getApplyTime());
                ret2 = storageService.agreementOut(id, storageOrder, oid, review, commoditys, weights, values, attrs);
            }
            if (!RestResult.isOk(ret2)) {
                return ret2;
            }
        }
        return ret;
    }

    /**
     * desc: 履约发货修改
     */
    public RestResult setShipped(int id, int oid, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values) {
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

        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_shipped, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成发货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createShippedComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("生成发货订单失败");
        }
        String msg = agreementOrderService.update(oid, comms, null);
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

        // 已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
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

        // 增加库存
        String msg = stockCloudService.handleAgreementStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, group.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
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
        // 存在出库单就不能撤销
        if (null != agreementStorageRepository.find(oid)) {
            return RestResult.fail("已出库的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_agreement_shipped);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!agreementOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        msg = stockCloudService.handleAgreementStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 履约退货
     */
    public RestResult returnc(int id, TAgreementOrder order, int pid, int sid, int review, int storage, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 发货单未审核不能退货
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到履约单信息");
        }
        if (!agreement.getOtype().equals(AGREEMENT_SHIPPED_ORDER.getValue())) {
            return RestResult.fail("履约单据类型异常");
        }
        if (null == agreement.getReview()) {
            return RestResult.fail("履约单未审核通过，不能进行退货");
        }

        order.setGid(agreement.getGid());
        order.setAid(agreement.getAid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_return, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createReturnComms(order, pid, commoditys, prices, weights, values, comms);
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

        // 添加关联
        if (!agreementReturnRepository.insert(oid, pid)) {
            return RestResult.fail("添加履约退货信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            RestResult ret2 = reviewReturn(id, oid);
            if (RestResult.isOk(ret2) && storage > 0) {
                // 一键入库
                if (sid <= 0) {
                    return RestResult.fail("未指定仓库，一键入库失败");
                }
                TStorageOrder storageOrder = new TStorageOrder();
                storageOrder.setOtype(STORAGE_AGREEMENT_IN_ORDER.getValue());
                storageOrder.setSid(sid);
                storageOrder.setTid(0);
                storageOrder.setApply(order.getApply());
                storageOrder.setApplyTime(order.getApplyTime());
                ret2 = storageService.agreementIn(id, storageOrder, oid, review, commoditys, weights, values, attrs);
            }
            if (!RestResult.isOk(ret2)) {
                return ret2;
            }
        }
        return ret;
    }

    /**
     * desc: 履约退货修改
     */
    public RestResult setReturn(int id, int oid, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values) {
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

        // 发货单
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到发货单信息");
        }

        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_agreement_return, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TAgreementCommodity>();
        ret = createReturnComms(order, pid, commoditys, prices, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        String msg = agreementOrderService.update(oid, comms, null);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delReturn(int id, int oid) {
        // 删除关联
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到发货单信息");
        }
        if (!agreementReturnRepository.delete(oid, pid)) {
            return RestResult.fail("删除履约退货信息失败");
        }
        return delShipped(id, oid);
    }

    public RestResult reviewReturn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 发货单
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到发货单信息");
        }

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
        order.setComplete(new Byte("1"));
        if (!agreementOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = stockCloudService.handleAgreementStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TAgreementOrder order = agreementOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }
        // 存在入库单就不能撤销
        if (null != agreementStorageRepository.find(oid)) {
            return RestResult.fail("已入库的订单不能撤销");
        }

        // 发货单
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到发货单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_agreement_return);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!agreementOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = stockCloudService.handleAgreementStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TAgreementOrder order, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(gid, reviewPerm, reviews);
    }

    private RestResult createShippedComms(TAgreementOrder order, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<TAgreementCommodity> list) {
        // 生成发货单
        int size = commoditys.size();
        if (size != weights.size() || size != norms.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            TAgreementCommodity c = new TAgreementCommodity();
            c.setCid(commoditys.get(i));
            c.setPrice(prices.get(i));
            c.setWeight(weights.get(i));
            c.setNorm(norms.get(i));
            c.setValue(values.get(i));
            list.add(c);

            total = total + c.getValue();
            price = price.add(c.getPrice());
        }
        order.setValue(total);
        order.setPrice(price);
        order.setCurValue(total);
        order.setCurPrice(price);
        return null;
    }

    private RestResult createReturnComms(TAgreementOrder order, int aid, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<TAgreementCommodity> list) {
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
                        return RestResult.fail("退货商品重量不能大于发货重量, 商品id:" + cid);
                    }
                    if (value > ac.getValue()) {
                        return RestResult.fail("退货商品件数不能大于发货件数, 商品id:" + cid);
                    }

                    TAgreementCommodity c = new TAgreementCommodity();
                    c.setCid(cid);
                    c.setPrice(prices.get(i));
                    c.setWeight(weight);
                    c.setNorm(ac.getNorm());
                    c.setValue(value);
                    list.add(c);

                    total = total + value;
                    price = price.add(c.getPrice());
                    break;
                }
            }
            if (!find) {
                return RestResult.fail("未查询到商品id:" + cid);
            }
        }
        order.setValue(total);
        order.setPrice(price);
        order.setCurValue(total);
        order.setCurPrice(price);
        return null;
    }

    // 获取退货对应的发货单
    private int getAgreementId(int id) {
        TAgreementReturn ret = agreementReturnRepository.find(id);
        if (null != ret) {
            return ret.getAid();
        }
        return 0;
    }
}
