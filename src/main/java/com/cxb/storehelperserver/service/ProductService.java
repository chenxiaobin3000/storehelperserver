package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
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

/**
 * desc: 生产业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductService {
    @Resource
    private CheckService checkService;

    @Resource
    private ProductOrderService productOrderService;

    @Resource
    private StockService stockService;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductOrderCommodityRepository productOrderCommodityRepository;

    @Resource
    private ProductOrderAttachmentRepository productOrderAttachmentRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private HalfgoodRepository halfgoodRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private DestroyRepository destroyRepository;

    @Resource
    private OrderReviewerRepository orderReviewerRepository;

    /**
     * desc: 生产开始
     */
    public RestResult process(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys,
                              List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_out_apply, mp_product_out_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成进货单
        val comms = new ArrayList<TProductOrderCommodity>();
        ret = createProductComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!productOrderRepository.insert(order)) {
            return RestResult.fail("生成进货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply userOrderApply = new TUserOrderApply();
        userOrderApply.setUid(id);
        userOrderApply.setOtype(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue());
        userOrderApply.setOid(oid);
        userOrderApply.setBatch(batch);
        if (!userOrderApplyRepository.insert(userOrderApply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview userOrderReview = new TUserOrderReview();
        userOrderReview.setOtype(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue());
        userOrderReview.setOid(oid);
        userOrderReview.setBatch(batch);
        for (Integer reviewer : reviews) {
            userOrderReview.setId(0);
            userOrderReview.setUid(reviewer);
            if (!userOrderReviewRepository.insert(userOrderReview)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产开始修改
     */
    public RestResult setProcess(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys,
                                 List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_out_apply, mp_product_out_apply, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        TProductOrder productOrder = productOrderRepository.find(order.getId());
        if (null == productOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != productOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 生成进货单
        val comms = new ArrayList<TProductOrderCommodity>();
        ret = createProductComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!productOrderRepository.update(order)) {
            return RestResult.fail("生成进货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delProcess(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 校验是否订单提交人，已经审核的订单，必须由审核人删除
        Integer review = order.getReview();
        if (null == review) {
            if (!order.getApply().equals(id)) {
                return RestResult.fail("订单必须由申请人删除");
            }
        } else {
            if (!review.equals(id)) {
                return RestResult.fail("订单必须由审核人删除");
            }
        }

        // 删除生效日期以后的所有库存记录
        val comms = productOrderCommodityRepository.find(oid);
        for (TProductOrderCommodity c : comms) {
            // 删除日期是制单日期的前一天
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(order.getApplyTime());
            calendar.add(Calendar.DATE, -1);
            stockService.delStock(TypeDefine.CommodityType.valueOf(c.getCtype()),
                    order.getSid(), c.getCid(), calendar.getTime());
        }
        if (!productOrderCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!productOrderAttachmentRepository.delete(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }

        if (null == review) {
            if (!userOrderApplyRepository.delete(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue(), oid)) {
                return RestResult.fail("删除订单申请人失败");
            }
            if (!userOrderReviewRepository.delete(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue(), oid)) {
                return RestResult.fail("删除订单审核人失败");
            }
        } else {
            if (!userOrderCompleteRepository.delete(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue(), oid)) {
                return RestResult.fail("删除完成订单失败");
            }
        }
        if (!productOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return RestResult.ok();
    }

    public RestResult reviewProcess(int id, int oid) {
        // 校验审核人员信息
        val reviews = userOrderReviewRepository.find(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue(), oid);
        boolean find = false;
        for (TUserOrderReview review : reviews) {
            if (review.getUid().equals(id)) {
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("您没有审核权限");
        }

        // 添加审核信息
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 删除apply和review信息
        if (!userOrderApplyRepository.delete(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue(), oid)) {
            return RestResult.fail("删除用户订单信息失败");
        }
        if (!userOrderReviewRepository.delete(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue(), oid)) {
            return RestResult.fail("添加用户订单审核信息失败");
        }
        // 插入complete信息
        TUserOrderComplete complete = new TUserOrderComplete();
        complete.setUid(id);
        complete.setOtype(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue());
        complete.setOid(oid);
        complete.setBatch(order.getBatch());
        if (!userOrderCompleteRepository.insert(complete)) {
            return RestResult.fail("完成用户订单审核信息失败");
        }
        return RestResult.ok();
    }

    public RestResult revokeProcess(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, product_getlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        val orderReviewers = orderReviewerRepository.find(gid);
        if (null == orderReviewers || orderReviewers.isEmpty()) {
            return RestResult.fail("未设置订单审核人，请联系系统管理员");
        }
        val reviews = new ArrayList<Integer>();
        for (TOrderReviewer orderReviewer : orderReviewers) {
            if (orderReviewer.getPid().equals(mp_product_out_review)) {
                reviews.add(orderReviewer.getUid());
            }
        }

        if (!userOrderCompleteRepository.delete(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue(), oid)) {
            return RestResult.fail("添加用户订单完成信息失败");
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply userOrderApply = new TUserOrderApply();
        userOrderApply.setUid(id);
        userOrderApply.setOtype(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue());
        userOrderApply.setOid(oid);
        userOrderApply.setBatch(batch);
        if (!userOrderApplyRepository.insert(userOrderApply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview userOrderReview = new TUserOrderReview();
        userOrderReview.setOtype(TypeDefine.OrderType.PRODUCT_OUT_ORDER.getValue());
        userOrderReview.setOid(oid);
        userOrderReview.setBatch(batch);
        for (Integer reviewer : reviews) {
            userOrderReview.setId(0);
            userOrderReview.setUid(reviewer);
            if (!userOrderReviewRepository.insert(userOrderReview)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产完成
     */
    public RestResult complete(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys,
                               List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_in_apply, mp_product_in_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成进货单
        val comms = new ArrayList<TProductOrderCommodity>();
        ret = createProductComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!productOrderRepository.insert(order)) {
            return RestResult.fail("生成进货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply userOrderApply = new TUserOrderApply();
        userOrderApply.setUid(id);
        userOrderApply.setOtype(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue());
        userOrderApply.setOid(oid);
        userOrderApply.setBatch(batch);
        if (!userOrderApplyRepository.insert(userOrderApply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview userOrderReview = new TUserOrderReview();
        userOrderReview.setOtype(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue());
        userOrderReview.setOid(oid);
        userOrderReview.setBatch(batch);
        for (Integer reviewer : reviews) {
            userOrderReview.setId(0);
            userOrderReview.setUid(reviewer);
            if (!userOrderReviewRepository.insert(userOrderReview)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产完成修改
     */
    public RestResult setComplete(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys,
                                  List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_in_apply, mp_product_in_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        TProductOrder productOrder = productOrderRepository.find(order.getId());
        if (null == productOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != productOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 生成进货单
        val comms = new ArrayList<TProductOrderCommodity>();
        ret = createProductComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!productOrderRepository.update(order)) {
            return RestResult.fail("生成进货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delComplete(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 校验是否订单提交人，已经审核的订单，必须由审核人删除
        Integer review = order.getReview();
        if (null == review) {
            if (!order.getApply().equals(id)) {
                return RestResult.fail("订单必须由申请人删除");
            }
        } else {
            if (!review.equals(id)) {
                return RestResult.fail("订单必须由审核人删除");
            }
        }

        // 删除生效日期以后的所有库存记录
        val comms = productOrderCommodityRepository.find(oid);
        for (TProductOrderCommodity c : comms) {
            // 删除日期是制单日期的前一天
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(order.getApplyTime());
            calendar.add(Calendar.DATE, -1);
            stockService.delStock(TypeDefine.CommodityType.valueOf(c.getCtype()),
                    order.getSid(), c.getCid(), calendar.getTime());
        }
        if (!productOrderCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!productOrderAttachmentRepository.delete(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }

        if (null == review) {
            if (!userOrderApplyRepository.delete(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue(), oid)) {
                return RestResult.fail("删除订单申请人失败");
            }
            if (!userOrderReviewRepository.delete(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue(), oid)) {
                return RestResult.fail("删除订单审核人失败");
            }
        } else {
            if (!userOrderCompleteRepository.delete(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue(), oid)) {
                return RestResult.fail("删除完成订单失败");
            }
        }
        if (!productOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return RestResult.ok();
    }

    public RestResult reviewComplete(int id, int oid) {
        // 校验审核人员信息
        val reviews = userOrderReviewRepository.find(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue(), oid);
        boolean find = false;
        for (TUserOrderReview review : reviews) {
            if (review.getUid().equals(id)) {
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("您没有审核权限");
        }

        // 添加审核信息
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 删除apply和review信息
        if (!userOrderApplyRepository.delete(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue(), oid)) {
            return RestResult.fail("删除用户订单信息失败");
        }
        if (!userOrderReviewRepository.delete(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue(), oid)) {
            return RestResult.fail("添加用户订单审核信息失败");
        }
        // 插入complete信息
        TUserOrderComplete complete = new TUserOrderComplete();
        complete.setUid(id);
        complete.setOtype(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue());
        complete.setOid(oid);
        complete.setBatch(order.getBatch());
        if (!userOrderCompleteRepository.insert(complete)) {
            return RestResult.fail("完成用户订单审核信息失败");
        }
        return RestResult.ok();
    }

    public RestResult revokeComplete(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, product_getlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        val orderReviewers = orderReviewerRepository.find(gid);
        if (null == orderReviewers || orderReviewers.isEmpty()) {
            return RestResult.fail("未设置订单审核人，请联系系统管理员");
        }
        val reviews = new ArrayList<Integer>();
        for (TOrderReviewer orderReviewer : orderReviewers) {
            if (orderReviewer.getPid().equals(mp_product_in_review)) {
                reviews.add(orderReviewer.getUid());
            }
        }

        if (!userOrderCompleteRepository.delete(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue(), oid)) {
            return RestResult.fail("添加用户订单完成信息失败");
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply userOrderApply = new TUserOrderApply();
        userOrderApply.setUid(id);
        userOrderApply.setOtype(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue());
        userOrderApply.setOid(oid);
        userOrderApply.setBatch(batch);
        if (!userOrderApplyRepository.insert(userOrderApply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview userOrderReview = new TUserOrderReview();
        userOrderReview.setOtype(TypeDefine.OrderType.PRODUCT_IN_ORDER.getValue());
        userOrderReview.setOid(oid);
        userOrderReview.setBatch(batch);
        for (Integer reviewer : reviews) {
            userOrderReview.setId(0);
            userOrderReview.setUid(reviewer);
            if (!userOrderReviewRepository.insert(userOrderReview)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TProductOrder order, int applyPerm, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermissionMp(id, applyPerm)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 验证审核人员信息
        val orderReviewers = orderReviewerRepository.find(gid);
        if (null == orderReviewers || orderReviewers.isEmpty()) {
            return RestResult.fail("未设置订单审核人，请联系系统管理员");
        }
        for (TOrderReviewer orderReviewer : orderReviewers) {
            if (orderReviewer.getPid().equals(reviewPerm)) {
                reviews.add(orderReviewer.getUid());
            }
        }
        if (reviews.isEmpty()) {
            return RestResult.fail("未设置进货订单审核人，请联系系统管理员");
        }
        return null;
    }

    private RestResult createProductComms(TProductOrder order, List<Integer> types, List<Integer> commoditys,
                                          List<Integer> values, List<BigDecimal> prices, List<TProductOrderCommodity> list) {
        // 生成进货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        int total = 0;
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
                case HALFGOOD:
                    THalfgood find2 = halfgoodRepository.find(cid);
                    if (null == find2) {
                        return RestResult.fail("未查询到半成品：" + cid);
                    }
                    unit = find2.getUnit();
                    break;
                case ORIGINAL:
                    TOriginal find3 = originalRepository.find(cid);
                    if (null == find3) {
                        return RestResult.fail("未查询到原料：" + cid);
                    }
                    unit = find3.getUnit();
                    break;
                case STANDARD:
                    TStandard find4 = standardRepository.find(cid);
                    if (null == find4) {
                        return RestResult.fail("未查询到标品：" + cid);
                    }
                    unit = find4.getUnit();
                    break;
                default:
                    TDestroy find5 = destroyRepository.find(cid);
                    if (null == find5) {
                        return RestResult.fail("未查询到废品：" + cid);
                    }
                    unit = find5.getUnit();
                    break;
            }

            // 生成数据
            TProductOrderCommodity c = new TProductOrderCommodity();
            c.setCtype(type.getValue());
            c.setCid(cid);
            c.setUnit(unit);
            c.setValue(values.get(i));
            c.setPrice(prices.get(i));
            list.add(c);
            total += values.get(i);
        }
        order.setValue(total);
        return null;
    }
}
