package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.COMMODITY;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.STANDARD;

/**
 * desc: 销售业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SaleService {
    @Resource
    private CheckService checkService;

    @Resource
    private SaleOrderService saleOrderService;

    @Resource
    private AgreementOrderService agreementOrderService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private SaleOrderRepository saleOrderRepository;

    @Resource
    private SaleCommodityRepository saleCommodityRepository;

    @Resource
    private SaleAttachmentRepository saleAttachmentRepository;

    @Resource
    private SaleRemarkRepository saleRemarkRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private MarketCommodityDetailRepository marketCommodityDetailRepository;

    @Resource
    private MarketStandardDetailRepository marketStandardDetailRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult sale(int id, TSaleOrder order) {
        int sid = order.getSid();
        int pid = order.getPid();
        Date date = order.getApplyTime();
        // 查找履约单
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到履约单信息");
        }
        int aid = agreement.getAid();
        int asid = agreement.getAsid();
        order.setAid(aid);
        order.setAsid(asid);

        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_sale_sale_apply, mp_sale_sale_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成销售单
        val sales = new ArrayList<TSaleCommodity>();
        val list = marketCommodityDetailRepository.sale(sid, aid, asid, date);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }
        for (MyMarketCommodity commodity : list) {
            TSaleCommodity c = new TSaleCommodity();
            sales.add(c);
            c.setCtype(COMMODITY.getValue());
            c.setCid(commodity.getCid());
            c.setPrice(commodity.getPrice());
            c.setValue(commodity.getValue());
        }

        val list2 = marketStandardDetailRepository.sale(sid, aid, asid, date);
        if (null == list2) {
            return RestResult.fail("未查询到销售信息");
        }
        for (MyMarketCommodity commodity : list2) {
            TSaleCommodity c = new TSaleCommodity();
            sales.add(c);
            c.setCtype(STANDARD.getValue());
            c.setCid(commodity.getCid());
            c.setPrice(commodity.getPrice());
            c.setValue(commodity.getValue());
        }

        // TODO 同个商品多个价格
        // 从履约单获取数据
        val agreementCommodities = agreementCommodityRepository.find(pid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到履约商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        val comms = new ArrayList<TSaleCommodity>();
        for (TSaleCommodity c : sales) {
            for (TAgreementCommodity c2 : agreementCommodities) {
                if (c.getCtype().equals(c2.getCtype()) && c.getCid().equals(c2.getCid())) {
                    if (c2.getCurValue() < c.getValue()) {
                        c.setValue(c2.getCurValue());
                    }
                    c.setPrice(c.getPrice().multiply(new BigDecimal(c.getValue())));

                    total = total + c.getValue();
                    price = price.add(c.getPrice());
                    comms.add(c);
                    break;
                }
            }
        }
        order.setValue(total);
        order.setPrice(price);

        // 生成售后单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!saleOrderRepository.insert(order)) {
            return RestResult.fail("生成售后订单失败");
        }
        int oid = order.getId();
        String msg = saleOrderService.update(oid, comms, null);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    public RestResult delSale(int id, int oid) {
        TSaleOrder order = saleOrderRepository.find(oid);
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
        saleAttachmentRepository.deleteByOid(oid);
        if (!saleCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!saleOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewSale(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 校验审核人员信息
        TSaleOrder order = saleOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }
        int pid = order.getPid();

        // 校验退货订单总件数不能超出采购单
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到对应的履约单");
        }
        int value = agreement.getCurValue() - order.getValue();
        if (value < 0) {
            return RestResult.fail("销售商品总量不能超出履约订单总量");
        }
        if (0 == value) {
            agreement.setComplete(new Byte("1"));
        }
        agreement.setCurValue(value);
        agreement.setCurPrice(agreement.getCurPrice().subtract(order.getPrice()));
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改履约单数据失败");
        }

        // TODO 同个商品多个价格
        // 更新履约单商品存量
        val saleCommodities = saleCommodityRepository.find(oid);
        if (null == saleCommodities || saleCommodities.isEmpty()) {
            return RestResult.fail("未查询到销售商品信息");
        }
        val agreementCommodities = agreementCommodityRepository.find(pid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到履约商品信息");
        }
        for (TSaleCommodity sc : saleCommodities) {
            for (TAgreementCommodity ac : agreementCommodities) {
                if (sc.getCtype().equals(ac.getCtype()) && sc.getCid().equals(ac.getCid())) {
                    ac.setCurValue(ac.getCurValue() - sc.getValue());
                }
            }
        }
        agreementCommodityRepository.update(agreementCommodities, pid);
        agreementOrderService.clean(pid);

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!saleOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeSale(int id, int oid) {
        TSaleOrder order = saleOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }
        int pid = order.getPid();

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, market_sale)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 还原扣除的履约单数量
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到对应的履约单");
        }
        agreement.setCurValue(agreement.getCurValue() + order.getValue());
        agreement.setCurPrice(agreement.getCurPrice().add(order.getPrice()));
        agreement.setComplete(new Byte("0"));
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改履约单数据失败");
        }

        // TODO 同个商品多个价格
        // 还原履约单商品存量
        val saleCommodities = saleCommodityRepository.find(oid);
        if (null == saleCommodities || saleCommodities.isEmpty()) {
            return RestResult.fail("未查询到销售商品信息");
        }
        val agreementCommodities = agreementCommodityRepository.find(pid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到履约商品信息");
        }
        for (TSaleCommodity sc : saleCommodities) {
            for (TAgreementCommodity ac : agreementCommodities) {
                if (sc.getCtype().equals(ac.getCtype()) && sc.getCid().equals(ac.getCid())) {
                    ac.setCurValue(ac.getCurValue() + sc.getValue());
                }
            }
        }
        agreementCommodityRepository.update(agreementCommodities, pid);
        agreementOrderService.clean(pid);

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_sale_sale_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!saleOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setSalePay(int id, int oid, BigDecimal pay) {
        // 校验审核人员信息
        TSaleOrder order = saleOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        order.setPayPrice(pay);
        if (!saleOrderRepository.update(order)) {
            return RestResult.fail("更新已收款信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 销售售后
     */
    public RestResult after(int id, TSaleOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_sale_after_apply, mp_sale_after_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成售后单
        val comms = new ArrayList<TSaleCommodity>();
        ret = createAfterComms(order, types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成售后单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!saleOrderRepository.insert(order)) {
            return RestResult.fail("生成售后订单失败");
        }
        int oid = order.getId();
        String msg = saleOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 销售售后修改
     */
    public RestResult setAfter(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TSaleOrder order = saleOrderRepository.find(oid);
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
        RestResult ret = check(id, order, mp_sale_after_apply, mp_sale_after_review, reviews);
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

        // 生成售后单
        val comms = new ArrayList<TSaleCommodity>();
        ret = createAfterComms(order, types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!saleOrderRepository.update(order)) {
            return RestResult.fail("生成售后订单失败");
        }
        String msg = saleOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delAfter(int id, int oid) {
        return delSale(id, oid);
    }

    public RestResult reviewAfter(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 校验审核人员信息
        TSaleOrder order = saleOrderRepository.find(oid);
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
        if (!saleOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeAfter(int id, int oid) {
        TSaleOrder order = saleOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, market_after)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_sale_after_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!saleOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 云仓损耗
     */
    public RestResult loss(int id, TSaleOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_sale_loss_apply, mp_sale_loss_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单
        val comms = new ArrayList<TSaleCommodity>();
        ret = createAfterComms(order, types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!saleOrderRepository.insert(order)) {
            return RestResult.fail("生成损耗订单失败");
        }
        int oid = order.getId();
        String msg = saleOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 云仓损耗修改
     */
    public RestResult setLoss(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TSaleOrder order = saleOrderRepository.find(oid);
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
        RestResult ret = check(id, order, mp_sale_loss_apply, mp_sale_loss_review, reviews);
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
        val comms = new ArrayList<TSaleCommodity>();
        ret = createAfterComms(order, types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!saleOrderRepository.update(order)) {
            return RestResult.fail("生成损耗订单失败");
        }
        String msg = saleOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delLoss(int id, int oid) {
        return delSale(id, oid);
    }

    public RestResult reviewLoss(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 校验审核人员信息
        TSaleOrder order = saleOrderRepository.find(oid);
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
        if (!saleOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeLoss(int id, int oid) {
        TSaleOrder order = saleOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, market_loss)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_sale_loss_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!saleOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TSaleOrder order, int applyPerm, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(id, gid, applyPerm, reviewPerm, reviews);
    }

    private RestResult createAfterComms(TSaleOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TSaleCommodity> list) {
        // 生成售后单
        int size = commoditys.size();
        if (size != types.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
/*        val agreementCommodities = agreementCommodityRepository.find(aid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到履约商品信息");
        }
        int sid = order.getSid();
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);

            TSaleCommodity c = new TSaleCommodity();
            c.setCtype(ctype);
            c.setCid(cid);
            if (weight == stock.getWeight()) {
                c.setPrice(stock.getPrice());
            } else {
                c.setPrice(stock.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(stock.getWeight()), 2, RoundingMode.DOWN));
            }
            c.setWeight(weight);
            c.setValue(value);
            list.add(c);

            total = total + weight;
            price = price.add(c.getPrice());
        }
        order.setUnit(total);
        order.setPrice(price);*/
        return null;
    }
}
