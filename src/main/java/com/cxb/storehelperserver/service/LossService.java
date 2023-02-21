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
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.LOSS_LOCAL_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.LOSS_CLOUD_ORDER;

/**
 * desc: 损耗业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class LossService {
    @Resource
    private CheckService checkService;

    @Resource
    private LossOrderService lossOrderService;

    @Resource
    private StockService stockService;

    @Resource
    private LossOrderRepository lossOrderRepository;

    @Resource
    private LossCommodityRepository lossCommodityRepository;

    @Resource
    private LossAttachmentRepository lossAttachmentRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private HalfgoodRepository halfgoodRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private OrderReviewerRepository orderReviewerRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 损耗进货
     */
    public RestResult loss(int id, TLossOrder order, List<Integer> types, List<Integer> commoditys,
                               List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_loss_local_apply, mp_loss_local_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单
        val comms = new ArrayList<TLossCommodity>();
        ret = createLossComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!lossOrderRepository.insert(order)) {
            return RestResult.fail("生成损耗订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = lossOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply apply = new TUserOrderApply();
        apply.setUid(id);
        apply.setGid(order.getGid());
        apply.setSid(order.getSid());
        apply.setOtype(LOSS_LOCAL_ORDER.getValue());
        apply.setOid(oid);
        apply.setBatch(batch);
        if (!userOrderApplyRepository.insert(apply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview review = new TUserOrderReview();
        review.setGid(order.getGid());
        review.setSid(order.getSid());
        review.setOtype(LOSS_LOCAL_ORDER.getValue());
        review.setOid(oid);
        review.setBatch(batch);
        for (Integer reviewer : reviews) {
            review.setId(0);
            review.setUid(reviewer);
            if (!userOrderReviewRepository.insert(review)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 原料损耗修改
     */
    public RestResult setLoss(int id, TLossOrder order, List<Integer> types, List<Integer> commoditys,
                                  List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_loss_local_apply, mp_loss_local_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        TLossOrder lossOrder = lossOrderRepository.find(order.getId());
        if (null == lossOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != lossOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!lossOrder.getSid().equals(order.getSid())) {
            val userOrderApply = userOrderApplyRepository.find(LOSS_LOCAL_ORDER.getValue(), order.getId());
            userOrderApply.setSid(order.getSid());
            if (!userOrderApplyRepository.update(userOrderApply)) {
                return RestResult.fail("修改用户订单信息失败");
            }

            val userOrderReviews = userOrderReviewRepository.find(LOSS_LOCAL_ORDER.getValue(), order.getId());
            for (TUserOrderReview review : userOrderReviews) {
                review.setSid(order.getSid());
                if (!userOrderReviewRepository.update(review)) {
                    return RestResult.fail("修改用户订单审核信息失败");
                }
            }
        }

        // 生成损耗单
        val comms = new ArrayList<TLossCommodity>();
        ret = createLossComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!lossOrderRepository.update(order)) {
            return RestResult.fail("生成损耗订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = lossOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delLoss(int id, int oid) {
        TLossOrder order = lossOrderRepository.find(oid);
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

        // 删除生效日期以后的所有库存记录，删除日期是制单日期的前一天
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(order.getApplyTime());
        calendar.add(Calendar.DATE, -1);
        stockService.delStock(order.getSid(), calendar.getTime());

        // 删除商品附件数据
        if (!lossCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!lossAttachmentRepository.delete(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }

        if (null == review) {
            if (!userOrderApplyRepository.delete(LOSS_LOCAL_ORDER.getValue(), oid)) {
                return RestResult.fail("删除订单申请人失败");
            }
            if (!userOrderReviewRepository.delete(LOSS_LOCAL_ORDER.getValue(), oid)) {
                return RestResult.fail("删除订单审核人失败");
            }
        } else {
            if (!userOrderCompleteRepository.delete(LOSS_LOCAL_ORDER.getValue(), oid)) {
                return RestResult.fail("删除完成订单失败");
            }
        }
        if (!lossOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return RestResult.ok();
    }

    public RestResult reviewLoss(int id, int oid) {
        // 校验审核人员信息
        val reviews = userOrderReviewRepository.find(LOSS_LOCAL_ORDER.getValue(), oid);
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

        // TODO 校验库存数量扣除后是否大于0

        // 添加审核信息
        TLossOrder order = lossOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!lossOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 删除apply和review信息
        if (!userOrderApplyRepository.delete(LOSS_LOCAL_ORDER.getValue(), oid)) {
            return RestResult.fail("删除用户订单信息失败");
        }
        if (!userOrderReviewRepository.delete(LOSS_LOCAL_ORDER.getValue(), oid)) {
            return RestResult.fail("添加用户订单审核信息失败");
        }

        // 插入complete信息
        TUserOrderComplete complete = new TUserOrderComplete();
        complete.setUid(id);
        complete.setGid(order.getGid());
        complete.setSid(order.getSid());
        complete.setOtype(LOSS_LOCAL_ORDER.getValue());
        complete.setOid(oid);
        complete.setBatch(order.getBatch());
        complete.setCdate(dateUtil.getStartTime(order.getApplyTime()));
        if (!userOrderCompleteRepository.insert(complete)) {
            return RestResult.fail("完成用户订单审核信息失败");
        }
        return RestResult.ok();
    }

    public RestResult revokeLoss(int id, int oid) {
        TLossOrder order = lossOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, loss_getlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        val orderReviewers = orderReviewerRepository.find(gid);
        if (null == orderReviewers || orderReviewers.isEmpty()) {
            return RestResult.fail("未设置订单审核人，请联系系统管理员");
        }
        val reviews = new ArrayList<Integer>();
        for (TOrderReviewer orderReviewer : orderReviewers) {
            if (orderReviewer.getPid().equals(mp_loss_local_review)) {
                reviews.add(orderReviewer.getUid());
            }
        }

        if (!userOrderCompleteRepository.delete(LOSS_LOCAL_ORDER.getValue(), oid)) {
            return RestResult.fail("添加用户订单完成信息失败");
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply apply = new TUserOrderApply();
        apply.setUid(id);
        apply.setGid(order.getGid());
        apply.setSid(order.getSid());
        apply.setOtype(LOSS_LOCAL_ORDER.getValue());
        apply.setOid(oid);
        apply.setBatch(batch);
        if (!userOrderApplyRepository.insert(apply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview review = new TUserOrderReview();
        review.setGid(order.getGid());
        review.setSid(order.getSid());
        review.setOtype(LOSS_LOCAL_ORDER.getValue());
        review.setOid(oid);
        review.setBatch(batch);
        for (Integer reviewer : reviews) {
            review.setId(0);
            review.setUid(reviewer);
            if (!userOrderReviewRepository.insert(review)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }

        // 撤销审核人信息
        if (!lossOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 原料退货
     */
    public RestResult returnc(int id, TLossOrder order, List<Integer> types, List<Integer> commoditys,
                              List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_loss_cloud_apply, mp_loss_cloud_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单
        val comms = new ArrayList<TLossCommodity>();
        ret = createLossComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!lossOrderRepository.insert(order)) {
            return RestResult.fail("生成损耗订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = lossOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply apply = new TUserOrderApply();
        apply.setUid(id);
        apply.setGid(order.getGid());
        apply.setSid(order.getSid());
        apply.setOtype(LOSS_CLOUD_ORDER.getValue());
        apply.setOid(oid);
        apply.setBatch(batch);
        if (!userOrderApplyRepository.insert(apply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview review = new TUserOrderReview();
        review.setGid(order.getGid());
        review.setSid(order.getSid());
        review.setOtype(LOSS_CLOUD_ORDER.getValue());
        review.setOid(oid);
        review.setBatch(batch);
        for (Integer reviewer : reviews) {
            review.setId(0);
            review.setUid(reviewer);
            if (!userOrderReviewRepository.insert(review)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 原料退货修改
     */
    public RestResult setReturn(int id, TLossOrder order, List<Integer> types, List<Integer> commoditys,
                                List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_loss_cloud_apply, mp_loss_cloud_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        TLossOrder lossOrder = lossOrderRepository.find(order.getId());
        if (null == lossOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != lossOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!lossOrder.getSid().equals(order.getSid())) {
            val userOrderApply = userOrderApplyRepository.find(LOSS_CLOUD_ORDER.getValue(), order.getId());
            userOrderApply.setSid(order.getSid());
            if (!userOrderApplyRepository.update(userOrderApply)) {
                return RestResult.fail("修改用户订单信息失败");
            }

            val userOrderReviews = userOrderReviewRepository.find(LOSS_CLOUD_ORDER.getValue(), order.getId());
            for (TUserOrderReview review : userOrderReviews) {
                review.setSid(order.getSid());
                if (!userOrderReviewRepository.update(review)) {
                    return RestResult.fail("修改用户订单审核信息失败");
                }
            }
        }

        // 生成损耗单
        val comms = new ArrayList<TLossCommodity>();
        ret = createLossComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!lossOrderRepository.update(order)) {
            return RestResult.fail("生成损耗订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = lossOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delReturn(int id, int oid) {
        TLossOrder order = lossOrderRepository.find(oid);
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

        // 删除生效日期以后的所有库存记录，删除日期是制单日期的前一天
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(order.getApplyTime());
        calendar.add(Calendar.DATE, -1);
        stockService.delStock(order.getSid(), calendar.getTime());

        // 删除商品附件数据
        if (!lossCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!lossAttachmentRepository.delete(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }

        if (null == review) {
            if (!userOrderApplyRepository.delete(LOSS_CLOUD_ORDER.getValue(), oid)) {
                return RestResult.fail("删除订单申请人失败");
            }
            if (!userOrderReviewRepository.delete(LOSS_CLOUD_ORDER.getValue(), oid)) {
                return RestResult.fail("删除订单审核人失败");
            }
        } else {
            if (!userOrderCompleteRepository.delete(LOSS_CLOUD_ORDER.getValue(), oid)) {
                return RestResult.fail("删除完成订单失败");
            }
        }
        if (!lossOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return RestResult.ok();
    }

    public RestResult reviewReturn(int id, int oid) {
        // 校验审核人员信息
        val reviews = userOrderReviewRepository.find(LOSS_CLOUD_ORDER.getValue(), oid);
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
        TLossOrder order = lossOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!lossOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 删除apply和review信息
        if (!userOrderApplyRepository.delete(LOSS_CLOUD_ORDER.getValue(), oid)) {
            return RestResult.fail("删除用户订单信息失败");
        }
        if (!userOrderReviewRepository.delete(LOSS_CLOUD_ORDER.getValue(), oid)) {
            return RestResult.fail("添加用户订单审核信息失败");
        }
        // 插入complete信息
        TUserOrderComplete complete = new TUserOrderComplete();
        complete.setUid(id);
        complete.setGid(order.getGid());
        complete.setSid(order.getSid());
        complete.setOtype(LOSS_CLOUD_ORDER.getValue());
        complete.setOid(oid);
        complete.setBatch(order.getBatch());
        complete.setCdate(dateUtil.getStartTime(order.getApplyTime()));
        if (!userOrderCompleteRepository.insert(complete)) {
            return RestResult.fail("完成用户订单审核信息失败");
        }
        return RestResult.ok();
    }

    public RestResult revokeReturn(int id, int oid) {
        TLossOrder order = lossOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, loss_getlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        val orderReviewers = orderReviewerRepository.find(gid);
        if (null == orderReviewers || orderReviewers.isEmpty()) {
            return RestResult.fail("未设置订单审核人，请联系系统管理员");
        }
        val reviews = new ArrayList<Integer>();
        for (TOrderReviewer orderReviewer : orderReviewers) {
            if (orderReviewer.getPid().equals(mp_loss_cloud_review)) {
                reviews.add(orderReviewer.getUid());
            }
        }

        if (!userOrderCompleteRepository.delete(LOSS_CLOUD_ORDER.getValue(), oid)) {
            return RestResult.fail("添加用户订单完成信息失败");
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply apply = new TUserOrderApply();
        apply.setUid(id);
        apply.setGid(order.getGid());
        apply.setSid(order.getSid());
        apply.setOtype(LOSS_CLOUD_ORDER.getValue());
        apply.setOid(oid);
        apply.setBatch(batch);
        if (!userOrderApplyRepository.insert(apply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview review = new TUserOrderReview();
        review.setGid(order.getGid());
        review.setSid(order.getSid());
        review.setOtype(LOSS_CLOUD_ORDER.getValue());
        review.setOid(oid);
        review.setBatch(batch);
        for (Integer reviewer : reviews) {
            review.setId(0);
            review.setUid(reviewer);
            if (!userOrderReviewRepository.insert(review)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }

        // 撤销审核人信息
        if (!lossOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TLossOrder order, int applyPerm, int reviewPerm, List<Integer> reviews) {
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
            return RestResult.fail("未设置损耗订单审核人，请联系系统管理员");
        }
        return null;
    }

    private RestResult createLossComms(TLossOrder order, List<Integer> types, List<Integer> commoditys,
                                          List<Integer> values, List<BigDecimal> prices, List<TLossCommodity> list) {
        // 生成损耗单
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
                default:
                    return RestResult.fail("商品类型异常：" + type);
            }

            // 生成数据
            TLossCommodity c = new TLossCommodity();
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
