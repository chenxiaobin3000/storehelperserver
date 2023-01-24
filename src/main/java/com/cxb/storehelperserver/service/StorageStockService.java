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
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageOrderCommodityRepository storageOrderCommodityRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductOrderCommodityRepository productOrderCommodityRepository;

    @Resource
    private ProductOrderAttachmentRepository productOrderAttachmentRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementOrderCommodityRepository agreementOrderCommodityRepository;

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
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 验证审核人员信息
        val orderReviewers = orderReviewerRepository.find(gid);
        if (null == orderReviewers || orderReviewers.isEmpty()) {
            return RestResult.fail("未设置订单审核人，请联系系统管理员");
        }
        val reviews = new ArrayList<Integer>();
        for (TOrderReviewer orderReviewer : orderReviewers) {
            if (orderReviewer.getPid().equals(OrderType.STORAGE_IN_ORDER.getValue())) {
                reviews.add(orderReviewer.getUid());
            }
        }
        if (reviews.isEmpty()) {
            return RestResult.fail("未设置进货订单审核人，请联系系统管理员");
        }

        // 生成进货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        int total = 0;
        val list = new ArrayList<TStorageOrderCommodity>();
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
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成进货订单失败");
        }
        int oid = order.getId();
        int sid = order.getSid();
        for (TStorageOrderCommodity c : list) {
            c.setOrid(oid);
            if (!storageOrderCommodityRepository.insert(c)) {
                return RestResult.fail("生成订单商品数据失败");
            }

            // 修改库存数量
            switch (CommodityType.valueOf(c.getCtype())) {
                case COMMODITY:
                    if (!updateCommodity(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                case HALFGOOD:
                    if (!updateHalfgood(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                case ORIGINAL:
                    if (!updateOriginal(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                case STANDARD:
                    if (!updateStandard(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                default:
                    if (!updateDestroy(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
            }
        }

        // 修改附件orderid
        for (Integer attr : attrs) {
            TProductOrderAttachment productOrderAttachment = productOrderAttachmentRepository.find(attr);
            if (null != productOrderAttachment) {
                productOrderAttachment.setOrid(oid);
                if (!productOrderAttachmentRepository.update(productOrderAttachment)) {
                    return RestResult.fail("添加订单附件失败");
                }
            }
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

    private boolean updateCommodity(int sid, int cid, int unit, int value) {
        TStorageCommodity storageCommodity = storageCommodityRepository.find(sid, cid);
        if (null == storageCommodity) {
            storageCommodity = new TStorageCommodity();
            storageCommodity.setSid(sid);
            storageCommodity.setCid(cid);
            storageCommodity.setValue(unit * value);
            if (!storageCommodityRepository.insert(storageCommodity)) {
                // TODO 测试写入失败回滚
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
            setOriginalCache(sid, cid, unit * value);
        } else {
            storageCommodity.setValue(storageCommodity.getValue() + (unit * value));
            if (!storageCommodityRepository.update(storageCommodity)) {
                // TODO 测试写入失败回滚
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
            setCommodityCache(sid, cid, unit * value);
        }
        return true;
    }

    private boolean updateHalfgood(int sid, int cid, int unit, int value) {
        return false;
    }

    private boolean updateOriginal(int sid, int cid, int unit, int value) {
        TStorageOriginal storageOriginal = storageOriginalRepository.find(sid, cid);
        if (null == storageOriginal) {
            storageOriginal = new TStorageOriginal();
            storageOriginal.setSid(sid);
            storageOriginal.setOid(cid);
            storageOriginal.setValue(unit * value);
            if (!storageOriginalRepository.insert(storageOriginal)) {
                // TODO 测试写入失败回滚
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
            setOriginalCache(sid, cid, unit * value);
        } else {
            storageOriginal.setValue(storageOriginal.getValue() + (unit * value));
            if (!storageOriginalRepository.update(storageOriginal)) {
                // TODO 测试写入失败回滚
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
            setOriginalCache(sid, cid, unit * value);
        }
        return true;
    }

    private boolean updateStandard(int sid, int cid, int unit, int value) {
        return false;
    }

    private boolean updateDestroy(int sid, int cid, int unit, int value) {
        return false;
    }
}
