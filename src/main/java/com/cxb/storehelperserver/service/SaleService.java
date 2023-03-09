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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.Permission.*;

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
    private CloudStockService cloudStockService;

    @Resource
    private SaleOrderRepository saleOrderRepository;

    @Resource
    private SaleCommodityRepository saleCommodityRepository;

    @Resource
    private SaleAttachmentRepository saleAttachmentRepository;

    @Resource
    private SaleRemarkRepository saleRemarkRepository;

    @Resource
    private CloudStockRepository cloudStockRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 销售售后
     */
    public RestResult returnc(int id, TSaleOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_sale_return_apply, mp_sale_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成售后单
        val comms = new ArrayList<TSaleCommodity>();
        ret = createReturnComms(order, types, commoditys, values, comms);
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
    public RestResult setReturn(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
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
        RestResult ret = check(id, order, mp_sale_return_apply, mp_sale_return_review, reviews);
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
        ret = createReturnComms(order, types, commoditys, values, comms);
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

    public RestResult delReturn(int id, int oid) {
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

    public RestResult reviewReturn(int id, int oid) {
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

        // 减少库存
        String msg = cloudStockService.handleSaleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
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
        if (!checkService.checkRolePermission(id, market_salereturn)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_sale_return_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!saleOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = cloudStockService.handleSaleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addReturnInfo(int id, int oid, String remark) {
        // 验证公司
        TSaleOrder order = saleOrderRepository.find(oid);
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
        saleOrderService.clean(oid);

        // 备注
        if (null != remark && remark.length() > 0) {
            if (!saleRemarkRepository.insert(oid, remark, new Date())) {
                return RestResult.fail("添加备注失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delReturnInfo(int id, int oid, int rid) {
        // 验证公司
        TSaleOrder order = saleOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        saleOrderService.clean(oid);

        // 备注由审核人删
        if (0 != rid) {
            if (!order.getReview().equals(rid)) {
                RestResult.fail("要删除备注，请联系订单审核人");
            }
            if (!saleRemarkRepository.delete(rid)) {
                return RestResult.fail("删除备注信息失败");
            }
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

    private RestResult createReturnComms(TSaleOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TSaleCommodity> list) {
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
            TCloudStock stock = cloudStockRepository.find(sid, ctype, cid);
            if (null == stock) {
                return RestResult.fail("未查询到库存类型:" + types.get(i) + ",商品:" + commoditys.get(i));
            }
            if (stock.getValue() < value) {
                return RestResult.fail("库存商品数量不足:" + types.get(i) + ",商品:" + commoditys.get(i));
            }

            // 生成数据
            TSaleCommodity c = new TSaleCommodity();
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
}
