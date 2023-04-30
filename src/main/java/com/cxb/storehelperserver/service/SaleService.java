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
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.SaleType.*;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.AGREEMENT_SHIPPED_ORDER;

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
    private ReviewService reviewService;

    @Resource
    private StockCloudService stockCloudService;

    @Resource
    private SaleOrderRepository saleOrderRepository;

    @Resource
    private SaleCommodityRepository saleCommodityRepository;

    @Resource
    private SaleAttachmentRepository saleAttachmentRepository;

    @Resource
    private SaleRemarkRepository saleRemarkRepository;

    @Resource
    private SaleLossRepository saleLossRepository;

    @Resource
    private SaleTypeRepository saleTypeRepository;

    @Resource
    private MarketCommodityDetailRepository marketCommodityDetailRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult getSaleType(int id, int gid) {
        val data = new HashMap<String, Object>();
        data.put("list", saleTypeRepository.findByGroup(gid));
        return RestResult.ok(data);
    }

    public RestResult sale(int id, TSaleOrder order, int review) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_sale_sale, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成销售单
        int gid = order.getGid();
        int sid = order.getSid();
        int aid = order.getAid();
        int asid = order.getAsid();
        Date date = order.getApplyTime();
        val sales = new ArrayList<TSaleCommodity>();
        val list = marketCommodityDetailRepository.sale(order.getSid(), aid, asid, date);
        if (null == list) {
            return RestResult.fail("未查询到销售信息");
        }
        for (MyMarketCommodity commodity : list) {
            TSaleCommodity c = new TSaleCommodity();
            sales.add(c);
            c.setCid(commodity.getCid());
            c.setPrice(commodity.getPrice());
            c.setValue(commodity.getValue());
        }

        // 从库存获取数据
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        val comms = new ArrayList<TSaleCommodity>();
        for (TSaleCommodity c : sales) {
            // 获取商品单位信息
            int cid = c.getCid();
            int weight = c.getWeight();
            int value = c.getValue();
            TStockCloudDay stock = stockCloudService.getStockCommodity(gid, sid, aid, asid, cid);
            if (null == stock) {
                return RestResult.fail("未查询到库存类型:" + cid);
            }
            if (weight > stock.getWeight()) {
                return RestResult.fail("库存商品重量不足:" + cid);
            }
            if (value > stock.getValue()) {
                return RestResult.fail("库存商品件数不足:" + cid);
            }

            if (stock.getValue() < c.getValue()) {
                c.setValue(stock.getValue());
            }
            c.setPrice(c.getPrice().multiply(new BigDecimal(c.getValue())));

            total = total + c.getValue();
            price = price.add(c.getPrice());
            comms.add(c);
        }
        order.setValue(total);
        order.setPrice(price);

        // 生成销售单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!saleOrderRepository.insert(order)) {
            return RestResult.fail("生成损耗订单失败");
        }
        int oid = order.getId();
        String msg = saleOrderService.update(oid, comms, null);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
    }

    public RestResult delSale(int id, int oid) {
        TSaleOrder order = saleOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
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

        // 校验审核人员信息
        TSaleOrder order = saleOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验库存

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!saleOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = stockCloudService.handleSaleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, group.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeSale(int id, int oid) {
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
        if (!checkService.checkRolePermission(id, market_sale)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_sale_sale);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!saleOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = stockCloudService.handleSaleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
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
     * desc: 销售损耗
     */
    public RestResult loss(int id, TSaleOrder order, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> values, List<Integer> attrs) {
        // 履约单未审核不能损耗
        int rid = order.getPid();
        TAgreementOrder agreement = agreementOrderRepository.find(rid);
        if (null == agreement) {
            return RestResult.fail("未查询到履约单信息");
        }
        if (!agreement.getOtype().equals(AGREEMENT_SHIPPED_ORDER.getValue())) {
            return RestResult.fail("履约单据类型异常");
        }
        if (null == agreement.getReview()) {
            return RestResult.fail("履约单未审核通过，不能进行损耗");
        }

        order.setGid(agreement.getGid());
        order.setSid(agreement.getSid());
        order.setAid(agreement.getAid());
        order.setAsid(agreement.getAsid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_sale_loss, reviews);
        if (null != ret) {
            return ret;
        }

        // 校验损耗类型
        val losses = saleTypeRepository.findByGroup(order.getGid());
        if (null == losses) {
            return RestResult.fail("未查询到损耗类型信息");
        }
        int tid = order.getTid();
        boolean find = false;
        for (TSaleType loss : losses) {
            if (loss.getId().equals(tid)) {
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("未查询到对应的损耗类型");
        }

        // 生成损耗单
        val comms = new ArrayList<TSaleCommodity>();
        ret = createLossComms(order, order.getPid(), commoditys, prices, values, comms);
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
     * desc: 销售损耗修改
     */
    public RestResult setLoss(int id, int oid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> values, List<Integer> attrs) {
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
        RestResult ret = check(id, order, mp_sale_loss, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TSaleCommodity>();
        ret = createLossComms(order, order.getPid(), commoditys, prices, values, comms);
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
        int pid = order.getPid();

        // 损耗类型
        val losses = saleTypeRepository.findByGroup(order.getGid());
        if (null == losses) {
            return RestResult.fail("未查询到损耗类型信息");
        }
        int tid = order.getTid();
        boolean find = false;
        int add = 0;
        for (TSaleType loss : losses) {
            if (loss.getId().equals(tid)) {
                add = loss.getIsAdd();
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("未查询到对应的损耗类型");
        }

        // 校验损耗订单总价格和总量不能超出履约单
        if (SALE_CONST.getValue() != add) {
            boolean saleAdd = SALE_ADD.getValue() == add;
            TAgreementOrder agreement = agreementOrderRepository.find(pid);
            if (null == agreement) {
                return RestResult.fail("未查询到对应的履约单");
            }
            int value = saleAdd ? agreement.getCurValue() + order.getValue() : agreement.getCurValue() - order.getValue();
            if (value < 0) {
                return RestResult.fail("损耗商品总件数不能超出履约订单总件数");
            }
            BigDecimal price = saleAdd ? agreement.getCurPrice().add(order.getPrice()) : agreement.getCurPrice().subtract(order.getPrice());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                return RestResult.fail("损耗商品总价不能超出履约订单总价");
            }
            if (0 == value) {
                agreement.setComplete(new Byte("1"));
            }
            if (saleAdd) {
                agreement.setComplete(new Byte("0"));
            }
            agreement.setCurValue(value);
            agreement.setCurPrice(price);
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
                    if (sc.getCid().equals(ac.getCid())) {
                        ac.setCurValue(saleAdd ? ac.getCurValue() + sc.getValue() : ac.getCurValue() - sc.getValue());
                    }
                }
            }
            agreementCommodityRepository.update(agreementCommodities, pid);
            agreementOrderService.clean(pid);
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!saleOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!saleLossRepository.insert(oid, order.getPid())) {
            return RestResult.fail("添加平台损耗信息失败");
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
        int pid = order.getPid();

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

        // 损耗类型
        val losses = saleTypeRepository.findByGroup(gid);
        if (null == losses) {
            return RestResult.fail("未查询到损耗类型信息");
        }
        int tid = order.getTid();
        boolean find = false;
        int add = 0;
        for (TSaleType loss : losses) {
            if (loss.getId().equals(tid)) {
                add = loss.getIsAdd();
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("未查询到对应的损耗类型");
        }

        // 还原扣除的履约单数量
        if (SALE_CONST.getValue() != add) {
            boolean saleAdd = SALE_ADD.getValue() == add;
            TAgreementOrder agreement = agreementOrderRepository.find(pid);
            if (null == agreement) {
                return RestResult.fail("未查询到对应的履约单");
            }
            int value = saleAdd ? agreement.getCurValue() - order.getValue() : agreement.getCurValue() + order.getValue();
            if (value < 0) {
                return RestResult.fail("损耗商品总件数不能超出履约订单总件数");
            }
            BigDecimal price = saleAdd ? agreement.getCurPrice().subtract(order.getPrice()) : agreement.getCurPrice().add(order.getPrice());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                return RestResult.fail("损耗商品总价不能超出履约订单总价");
            }
            if (0 == value) {
                agreement.setComplete(new Byte("1"));
            }
            if (!saleAdd) {
                agreement.setComplete(new Byte("0"));
            }
            agreement.setCurValue(value);
            agreement.setCurPrice(price);
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
                    if (sc.getCid().equals(ac.getCid())) {
                        ac.setCurValue(saleAdd ? ac.getCurValue() - sc.getValue() : ac.getCurValue() + sc.getValue());
                    }
                }
            }
            agreementCommodityRepository.update(agreementCommodities, pid);
            agreementOrderService.clean(pid);
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_sale_loss);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!agreementOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!saleLossRepository.delete(oid)) {
            return RestResult.fail("删除平台损耗信息失败");
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TSaleOrder order, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(gid, reviewPerm, reviews);
    }

    private RestResult createLossComms(TSaleOrder order, int aid, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> values, List<TSaleCommodity> list) {
        // 生成损耗单
        int size = commoditys.size();
        if (size != prices.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val agreementCommodities = agreementCommodityRepository.find(aid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到履约商品信息");
        }
        int all = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int cid = commoditys.get(i);
            int value = values.get(i);
            for (TAgreementCommodity ac : agreementCommodities) {
                if (ac.getCid() == cid) {
                    find = true;
                    if (value > ac.getValue()) {
                        return RestResult.fail("退货商品件数不能大于发货件数:" + cid);
                    }

                    TSaleCommodity c = new TSaleCommodity();
                    c.setCid(cid);
                    c.setPrice(prices.get(i));
                    c.setValue(value);
                    list.add(c);

                    all = all + value;
                    price = price.add(c.getPrice());
                    break;
                }
            }
            if (!find) {
                return RestResult.fail("未查询到商品id:" + cid);
            }
        }
        order.setValue(all);
        order.setPrice(price);
        return null;
    }
}
