package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.StorageCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType;

/**
 * desc: 仓库库存业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageStockService extends StorageCache {
    @Resource
    private CheckService checkService;

    @Resource
    private StorageCacheService storageCacheService;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;

    @Resource
    private StorageHalfgoodRepository storageHalfgoodRepository;

    @Resource
    private StorageOriginalRepository storageOriginalRepository;

    @Resource
    private StorageStandardRepository storageStandardRepository;

    @Resource
    private StorageDestroyRepository storageDestroyRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private OrderReviewerRepository orderReviewerRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private HalfgoodRepository halfgoodRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private StandardRepository standardRepository;

    /*
     * desc: 原料进货
     */
    public RestResult purchase(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                               List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_in_apply, mp_storage_in_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成进货单
        val list = new ArrayList<TStorageOrderCommodity>();
        ret = createStorageComms(order, types, commoditys, values, prices, list);
        if (null != ret) {
            return ret;
        }

        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成进货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        for (TStorageOrderCommodity c : list) {
            c.setOid(oid);
        }
        String msg = storageCacheService.update(oid, list, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 修改库存数量
        ret = updateStorageCache(order.getSid(), list);
        if (null != ret) {
            return ret;
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply userOrderApply = new TUserOrderApply();
        userOrderApply.setUid(id);
        userOrderApply.setOtype(OrderType.STORAGE_IN_ORDER.getValue());
        userOrderApply.setOid(oid);
        userOrderApply.setBatch(batch);
        if (!userOrderApplyRepository.insert(userOrderApply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview userOrderReview = new TUserOrderReview();
        userOrderReview.setOtype(OrderType.STORAGE_IN_ORDER.getValue());
        userOrderReview.setOid(oid);
        userOrderReview.setBatch(batch);
        for (Integer reviewer : reviews) {
            userOrderReview.setId(0);
            userOrderReview.setUid(reviewer);
        }
        return RestResult.ok();
    }

    /**
     * desc: 原料进货修改
     */
    public RestResult setPurchase(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                                  List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_in_apply, mp_storage_in_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成进货单
        val list = new ArrayList<TStorageOrderCommodity>();
        ret = createStorageComms(order, types, commoditys, values, prices, list);
        if (null != ret) {
            return ret;
        }

        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成进货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        for (TStorageOrderCommodity c : list) {
            c.setOid(oid);
        }
        String msg = storageCacheService.update(oid, list, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 修改库存数量
        ret = updateStorageCache(order.getSid(), list);
        if (null != ret) {
            return ret;
        }
        return RestResult.ok();
    }

    public RestResult delPurchase(int id, int oid) {
        return RestResult.ok();
    }

    /*
     * desc: 生产开始
     */
    public RestResult process(int id, int gid, int sid, List<Integer> commoditys, List<Integer> values, List<String> prices) {
        return RestResult.ok();
    }

    /*
     * desc: 生产完成
     */
    public RestResult complete(int id, int gid, int sid, List<Integer> commoditys, List<Integer> values, List<String> prices) {
        return RestResult.ok();
    }

    private RestResult check(int id, TStorageOrder order, int applyPerm, int reviewPerm, List<Integer> reviews) {
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

    private RestResult createStorageComms(TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                                          List<Integer> values, List<BigDecimal> prices, List<TStorageOrderCommodity> list) {
        // 生成进货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        int total = 0;
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            CommodityType type = CommodityType.valueOf(types.get(i));
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
            }

            // 生成数据
            TStorageOrderCommodity c = new TStorageOrderCommodity();
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

    private RestResult updateStorageCache(int sid, List<TStorageOrderCommodity> list) {
        for (TStorageOrderCommodity c : list) {
            switch (CommodityType.valueOf(c.getCtype())) {
                case COMMODITY:
                    if (!setCommodityCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                case HALFGOOD:
                    if (!setHalfgoodCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改半成品库存数据失败");
                    }
                    break;
                case ORIGINAL:
                    if (!setOriginalCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改原料库存数据失败");
                    }
                    break;
                case STANDARD:
                    if (!setStandardCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改标品库存数据失败");
                    }
                    break;
                default:
                    if (!setDestroyCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改废料库存数据失败");
                    }
                    break;
            }
        }
        return null;
    }
}
