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
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.OFFLINE_OFFLINE_ORDER;

/**
 * desc: 线下销售业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class OfflineService {
    @Resource
    private CheckService checkService;

    @Resource
    private OfflineOrderService offlineOrderService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private StorageService storageService;

    @Resource
    private OfflineOrderRepository offlineOrderRepository;

    @Resource
    private OfflineCommodityRepository offlineCommodityRepository;

    @Resource
    private OfflineAttachmentRepository offlineAttachmentRepository;

    @Resource
    private OfflineRemarkRepository offlineRemarkRepository;

    @Resource
    private OfflineReturnRepository offlineReturnRepository;

    @Resource
    private MarketCommodityDetailRepository marketCommodityDetailRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 线下销售
     */
    public RestResult offline(int id, TOfflineOrder order, int sid, int review, int storage, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_offline_offline, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成销售单
        val comms = new ArrayList<TOfflineCommodity>();
        ret = createOfflineComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成销售单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!offlineOrderRepository.insert(order)) {
            return RestResult.fail("生成销售订单失败");
        }
        int oid = order.getId();
        String msg = offlineOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            ret = reviewOffline(id, oid);
            if (RestResult.isOk(ret) && storage > 0) {
                // 一键出库
                if (sid <= 0) {
                    return RestResult.fail("未指定仓库，一键出库失败");
                }
                TStorageOrder storageOrder = new TStorageOrder();
                storageOrder.setSid(sid);
                storageOrder.setTid(0);
                storageOrder.setApply(order.getApply());
                return storageService.purchaseOut(id, storageOrder, oid, review, commoditys, weights, values, attrs);
            }
        }
        return ret;
    }

    /**
     * desc: 线下销售修改
     */
    public RestResult setOffline(int id, int oid, int aid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices,
                                 List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TOfflineOrder order = offlineOrderRepository.find(oid);
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
        RestResult ret = check(id, order, mp_offline_offline, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成销售单
        val comms = new ArrayList<TOfflineCommodity>();
        ret = createOfflineComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!offlineOrderRepository.update(order)) {
            return RestResult.fail("生成销售订单失败");
        }
        String msg = offlineOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delOffline(int id, int oid) {
        TOfflineOrder order = offlineOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 校验是否订单提交人，已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
        }

        // 删除商品附件数据
        offlineAttachmentRepository.deleteByOid(oid);
        if (!offlineCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!offlineOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewOffline(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TOfflineOrder order = offlineOrderRepository.find(oid);
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
        if (!offlineOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeOffline(int id, int oid) {
        TOfflineOrder order = offlineOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // TODO 存在入库单就不能改

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, offline_offline)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_offline_offline);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!offlineOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setOfflinePay(int id, int oid, BigDecimal pay) {
        // 校验审核人员信息
        TOfflineOrder order = offlineOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        order.setPayPrice(pay);
        if (!offlineOrderRepository.update(order)) {
            return RestResult.fail("更新已付款信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 销售退货
     */
    public RestResult returnc(int id, TOfflineOrder order, int pid, int sid, int review, int storage, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 销售单未审核不能退货
        TOfflineOrder offline = offlineOrderRepository.find(pid);
        if (null == offline) {
            return RestResult.fail("未查询到销售单");
        }
        if (!offline.getOtype().equals(OFFLINE_OFFLINE_ORDER.getValue())) {
            return RestResult.fail("销售单据类型异常");
        }
        if (null == offline.getReview()) {
            return RestResult.fail("销售单未审核通过，不能进行退货");
        }

        order.setGid(offline.getGid());
        order.setAid(offline.getAid());
        order.setAsid(offline.getAsid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_offline_return, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TOfflineCommodity>();
        ret = createReturnComms(order, pid, commoditys, prices, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成退货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!offlineOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        int oid = order.getId();
        String msg = offlineOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加关联
        if (!offlineReturnRepository.insert(oid, pid)) {
            return RestResult.fail("添加销售退货信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            ret = reviewReturn(id, oid);
            if (RestResult.isOk(ret) && storage > 0) {
                // 一键入库
                if (sid <= 0) {
                    return RestResult.fail("未指定仓库，一键入库失败");
                }
                TStorageOrder storageOrder = new TStorageOrder();
                storageOrder.setSid(sid);
                storageOrder.setTid(0);
                storageOrder.setApply(order.getApply());
                return storageService.purchaseIn(id, storageOrder, oid, review, commoditys, weights, values, attrs);
            }
        }
        return ret;
    }

    /**
     * desc: 销售退货修改
     */
    public RestResult setReturn(int id, int oid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TOfflineOrder order = offlineOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        // 销售单
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到销售单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_offline_return, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TOfflineCommodity>();
        ret = createReturnComms(order, pid, commoditys, prices, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!offlineOrderRepository.update(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        String msg = offlineOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delReturn(int id, int oid) {
        // 删除关联
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到销售单信息");
        }
        if (!offlineReturnRepository.delete(oid, pid)) {
            return RestResult.fail("删除销售退货信息失败");
        }
        return delOffline(id, oid);
    }

    public RestResult reviewReturn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 销售单
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到销售单信息");
        }

        // 校验审核人员信息
        TOfflineOrder order = offlineOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出采购单
        TOfflineOrder offline = offlineOrderRepository.find(pid);
        if (null == offline) {
            return RestResult.fail("未查询到对应的销售单");
        }
        int value = offline.getCurValue() - order.getValue();
        if (value < 0) {
            return RestResult.fail("退货商品总量不能超出销售订单总量");
        }
        BigDecimal price = offline.getCurPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("退货商品总价不能超出销售订单总价");
        }
        if (0 == value) {
            offline.setComplete(new Byte("1"));
        }
        offline.setCurValue(value);
        offline.setCurPrice(price);
        if (!offlineOrderRepository.update(offline)) {
            return RestResult.fail("修改销售单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!offlineOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TOfflineOrder order = offlineOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // TODO 存在入库单就不能改

        // 销售单
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到销售单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, offline_return)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 还原扣除的采购单数量
        TOfflineOrder offline = offlineOrderRepository.find(pid);
        if (null == offline) {
            return RestResult.fail("未查询到对应的销售单");
        }
        offline.setCurValue(offline.getCurValue() + order.getValue());
        offline.setCurPrice(offline.getCurPrice().add(order.getPrice()));
        offline.setComplete(new Byte("0"));
        if (!offlineOrderRepository.update(offline)) {
            return RestResult.fail("修改销售单数据失败");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_offline_return);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!offlineOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TOfflineOrder order, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(gid, reviewPerm, reviews);
    }

    private RestResult createOfflineComms(TOfflineOrder order, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<TOfflineCommodity> list) {
        // 生成发货单
        int size = commoditys.size();
        if (size != weights.size() || size != norms.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            TOfflineCommodity c = new TOfflineCommodity();
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

    private RestResult createReturnComms(TOfflineOrder order, int aid, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<TOfflineCommodity> list) {
        // 生成销售单
        int size = commoditys.size();
        if (size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val offlineCommodities = offlineCommodityRepository.find(aid);
        if (null == offlineCommodities || offlineCommodities.isEmpty()) {
            return RestResult.fail("未查询到销售商品信息");
        }
        int total = 0;
        int all = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TOfflineCommodity ac : offlineCommodities) {
                if (ac.getCid() == cid) {
                    find = true;
                    if (weight > ac.getWeight()) {
                        return RestResult.fail("退货商品重量不能大于销售重量, 商品id:" + cid);
                    }
                    if (value > ac.getValue()) {
                        return RestResult.fail("退货商品件数不能大于销售件数, 商品id:" + cid);
                    }

                    TOfflineCommodity c = new TOfflineCommodity();
                    c.setCid(cid);
                    c.setPrice(prices.get(i));
                    c.setWeight(weight);
                    c.setNorm(ac.getNorm());
                    c.setValue(value);
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
        order.setValue(all);
        order.setPrice(price);
        order.setCurValue(all);
        order.setCurPrice(price);
        return null;
    }

    // 获取退货对应的销售单
    private int getOfflineId(int id) {
        TOfflineReturn ret = offlineReturnRepository.find(id);
        if (null != ret) {
            return ret.getSid();
        }
        return 0;
    }
}
