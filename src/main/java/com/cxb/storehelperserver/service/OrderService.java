package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.service.model.PageData;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.cxb.storehelperserver.util.TypeDefine.BusinessType;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 订单业务
 * auth: cxb
 * date: 2023/1/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderService {
    @Resource
    private CheckService checkService;

    @Resource
    private AgreementOrderService agreementOrderService;

    @Resource
    private ProductOrderService productOrderService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private StorageOrderService storageOrderService;

    @Resource
    private SaleOrderService saleOrderService;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementFareRepository agreementFareRepository;

    @Resource
    private AgreementRemarkRepository agreementRemarkRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductRemarkRepository productRemarkRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseFareRepository purchaseFareRepository;

    @Resource
    private PurchaseRemarkRepository purchaseRemarkRepository;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageFareRepository storageFareRepository;

    @Resource
    private StorageRemarkRepository storageRemarkRepository;

    @Resource
    private SaleOrderRepository saleOrderRepository;

    @Resource
    private SaleRemarkRepository saleRemarkRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private MarketAccountRepository marketAccountRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult addOrderFare(int id, BusinessType otype, int oid, String ship, String code, String phone, BigDecimal fare, String remark) {
        switch (otype) {
            case BUSINESS_AGREEMENT: {
                // 验证公司
                TAgreementOrder order = agreementOrderRepository.find(oid);
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
                agreementOrderService.clean(oid);

                // 运费
                if (!agreementFareRepository.insert(oid, ship, code, phone, fare, remark, new Date())) {
                    return RestResult.fail("添加物流费用失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_PURCHASE: {
                // 验证公司
                TPurchaseOrder order = purchaseOrderRepository.find(oid);
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
                purchaseOrderService.clean(oid);

                // 运费
                if (!purchaseFareRepository.insert(oid, ship, code, phone, fare, remark, new Date())) {
                    return RestResult.fail("添加物流费用失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_STORAGE: {
                // 验证公司
                TStorageOrder order = storageOrderRepository.find(oid);
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
                storageOrderService.clean(oid);

                // 运费
                if (!storageFareRepository.insert(oid, ship, code, phone, fare, remark, new Date())) {
                    return RestResult.fail("添加物流费用失败");
                }
                return RestResult.ok();
            }
            default:
                break;
        }
        return RestResult.fail("不支持添加运费");
    }

    public RestResult delOrderFare(int id, BusinessType otype, int oid, int fid) {
        switch (otype) {
            case BUSINESS_AGREEMENT: {
                // 验证公司
                TAgreementOrder order = agreementOrderRepository.find(oid);
                if (null == order) {
                    return RestResult.fail("未查询到订单信息");
                }
                String msg = checkService.checkGroup(id, order.getGid());
                if (null != msg) {
                    return RestResult.fail(msg);
                }
                agreementOrderService.clean(oid);

                // 运费由申请人删，已审核由审核人删，备注由审核人删
                TAgreementFare fare = agreementFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (null != fare.getReview()) {
                    if (!fare.getReview().equals(id)) {
                        return RestResult.fail("要删除已审核信息，请联系审核人");
                    }
                } else {
                    if (!order.getApply().equals(id)) {
                        return RestResult.fail("只能由申请人删除信息");
                    }
                }
                if (!agreementFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_PURCHASE: {
                // 验证公司
                TPurchaseOrder order = purchaseOrderRepository.find(oid);
                if (null == order) {
                    return RestResult.fail("未查询到订单信息");
                }
                String msg = checkService.checkGroup(id, order.getGid());
                if (null != msg) {
                    return RestResult.fail(msg);
                }
                purchaseOrderService.clean(oid);

                // 运费由申请人删，已审核由审核人删，备注由审核人删
                TPurchaseFare fare = purchaseFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (null != fare.getReview()) {
                    if (!fare.getReview().equals(id)) {
                        return RestResult.fail("要删除已审核信息，请联系审核人");
                    }
                } else {
                    if (!order.getApply().equals(id)) {
                        return RestResult.fail("只能由申请人删除信息");
                    }
                }
                if (!purchaseFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_STORAGE: {
                // 验证公司
                TStorageOrder order = storageOrderRepository.find(oid);
                if (null == order) {
                    return RestResult.fail("未查询到订单信息");
                }
                String msg = checkService.checkGroup(id, order.getGid());
                if (null != msg) {
                    return RestResult.fail(msg);
                }
                storageOrderService.clean(oid);

                // 运费由申请人删，已审核由审核人删，备注由审核人删
                TStorageFare fare = storageFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (null != fare.getReview()) {
                    if (!fare.getReview().equals(id)) {
                        return RestResult.fail("要删除已审核信息，请联系审核人");
                    }
                } else {
                    if (!order.getApply().equals(id)) {
                        return RestResult.fail("只能由申请人删除信息");
                    }
                }
                if (!storageFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
                return RestResult.ok();
            }
            default:
                break;
        }
        return RestResult.fail("不支持添加运费");
    }

    public RestResult addOrderRemark(int id, BusinessType otype, int oid, String remark) {
        switch (otype) {
            case BUSINESS_AGREEMENT: {
                // 验证公司
                TAgreementOrder order = agreementOrderRepository.find(oid);
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
                agreementOrderService.clean(oid);

                // 备注
                if (!agreementRemarkRepository.insert(oid, remark, new Date())) {
                    return RestResult.fail("添加备注失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_PRODUCT: {
                // 验证公司
                TProductOrder order = productOrderRepository.find(oid);
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
                productOrderService.clean(oid);

                // 备注
                if (null != remark && remark.length() > 0) {
                    if (!productRemarkRepository.insert(oid, remark, new Date())) {
                        return RestResult.fail("添加备注失败");
                    }
                }
                return RestResult.ok();
            }
            case BUSINESS_PURCHASE: {
                // 验证公司
                TPurchaseOrder order = purchaseOrderRepository.find(oid);
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
                purchaseOrderService.clean(oid);

                // 备注
                if (!purchaseRemarkRepository.insert(oid, remark, new Date())) {
                    return RestResult.fail("添加备注失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_SALE: {
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
                if (!saleRemarkRepository.insert(oid, remark, new Date())) {
                    return RestResult.fail("添加备注失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_STORAGE: {
                // 验证公司
                TStorageOrder order = storageOrderRepository.find(oid);
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
                storageOrderService.clean(oid);

                // 备注
                if (!storageRemarkRepository.insert(oid, remark, new Date())) {
                    return RestResult.fail("添加备注失败");
                }
                return RestResult.ok();
            }
            default:
                break;
        }
        return RestResult.fail("不支持添加运费");
    }

    public RestResult delOrderRemark(int id, BusinessType otype, int oid, int rid) {
        switch (otype) {
            case BUSINESS_AGREEMENT: {
                // 验证公司
                TAgreementOrder order = agreementOrderRepository.find(oid);
                if (null == order) {
                    return RestResult.fail("未查询到订单信息");
                }
                String msg = checkService.checkGroup(id, order.getGid());
                if (null != msg) {
                    return RestResult.fail(msg);
                }
                agreementOrderService.clean(oid);
                if (!order.getReview().equals(rid)) {
                    RestResult.fail("要删除备注，请联系订单审核人");
                }
                if (!agreementRemarkRepository.delete(rid)) {
                    return RestResult.fail("删除备注信息失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_PRODUCT: {
                // 验证公司
                TProductOrder order = productOrderRepository.find(oid);
                if (null == order) {
                    return RestResult.fail("未查询到订单信息");
                }
                String msg = checkService.checkGroup(id, order.getGid());
                if (null != msg) {
                    return RestResult.fail(msg);
                }
                productOrderService.clean(oid);

                // 备注由审核人删
                if (!order.getReview().equals(rid)) {
                    RestResult.fail("要删除备注，请联系订单审核人");
                }
                if (!productRemarkRepository.delete(rid)) {
                    return RestResult.fail("删除备注信息失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_PURCHASE: {
                // 验证公司
                TPurchaseOrder order = purchaseOrderRepository.find(oid);
                if (null == order) {
                    return RestResult.fail("未查询到订单信息");
                }
                String msg = checkService.checkGroup(id, order.getGid());
                if (null != msg) {
                    return RestResult.fail(msg);
                }
                purchaseOrderService.clean(oid);

                // 备注由审核人删
                if (!order.getReview().equals(rid)) {
                    RestResult.fail("要删除备注，请联系订单审核人");
                }
                if (!purchaseRemarkRepository.delete(rid)) {
                    return RestResult.fail("删除备注信息失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_SALE: {
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
                if (!order.getReview().equals(rid)) {
                    RestResult.fail("要删除备注，请联系订单审核人");
                }
                if (!saleRemarkRepository.delete(rid)) {
                    return RestResult.fail("删除备注信息失败");
                }
                return RestResult.ok();
            }
            case BUSINESS_STORAGE: {
                // 验证公司
                TStorageOrder order = storageOrderRepository.find(oid);
                if (null == order) {
                    return RestResult.fail("未查询到订单信息");
                }
                String msg = checkService.checkGroup(id, order.getGid());
                if (null != msg) {
                    return RestResult.fail(msg);
                }
                storageOrderService.clean(oid);

                // 备注由审核人删
                if (!order.getReview().equals(rid)) {
                    RestResult.fail("要删除备注，请联系订单审核人");
                }
                if (!storageRemarkRepository.delete(rid)) {
                    return RestResult.fail("删除备注信息失败");
                }
                return RestResult.ok();
            }
            default:
                break;
        }
        return RestResult.fail("不支持添加运费");
    }

    public RestResult getAgreementOrder(int id, int type, int page, int limit, ReviewType review, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = agreementOrderRepository.total(group.getGid(), type, review, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = agreementOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TAgreementOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getAid(), o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                if (null != datas) {
                    ret.put("comms", datas.get("comms"));
                    ret.put("attrs", datas.get("attrs"));
                    ret.put("fares", datas.get("fares"));
                    ret.put("total", datas.get("total"));
                    ret.put("remarks", datas.get("remarks"));
                }
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getProductOrder(int id, int type, int page, int limit, ReviewType review, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = productOrderRepository.total(group.getGid(), type, review, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = productOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TProductOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), null);
                HashMap<String, Object> datas = productOrderService.find(o.getId());
                if (null != datas) {
                    ret.put("comms", datas.get("comms"));
                    ret.put("attrs", datas.get("attrs"));
                    ret.put("fares", datas.get("fares"));
                    ret.put("remarks", datas.get("remarks"));
                }
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getPurchaseOrder(int id, int type, int page, int limit, ReviewType review, int complete, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = purchaseOrderRepository.total(group.getGid(), type, review, complete, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = purchaseOrderRepository.pagination(group.getGid(), type, page, limit, review, complete, search);
        if (null != list && !list.isEmpty()) {
            for (TPurchaseOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getPayPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                HashMap<String, Object> datas = purchaseOrderService.find(o.getId());
                if (null != datas) {
                    ret.put("comms", datas.get("comms"));
                    ret.put("attrs", datas.get("attrs"));
                    ret.put("fares", datas.get("fares"));
                    ret.put("total", datas.get("total"));
                    ret.put("remarks", datas.get("remarks"));
                }
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getStorageOrder(int id, int type, int page, int limit, ReviewType review, int complete, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = storageOrderRepository.total(group.getGid(), type, review, complete, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = storageOrderRepository.pagination(group.getGid(), type, page, limit, review, complete, search);
        if (null != list && !list.isEmpty()) {
            for (TStorageOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                HashMap<String, Object> datas = storageOrderService.find(o.getId());
                if (null != datas) {
                    ret.put("comms", datas.get("comms"));
                    ret.put("attrs", datas.get("attrs"));
                    ret.put("fares", datas.get("fares"));
                    ret.put("total", datas.get("total"));
                    ret.put("remarks", datas.get("remarks"));
                }
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getSaleOrder(int id, int type, int page, int limit, ReviewType review, int complete, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = saleOrderRepository.total(group.getGid(), type, review, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = saleOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TSaleOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, null, o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getPayPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), null);
                HashMap<String, Object> datas = saleOrderService.find(o.getId());
                if (null != datas) {
                    ret.put("comms", datas.get("comms"));
                    ret.put("attrs", datas.get("attrs"));
                    ret.put("fares", datas.get("fares"));
                    ret.put("total", datas.get("total"));
                    ret.put("remarks", datas.get("remarks"));
                }
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getMyWait(int id, int page, int limit, String search) {
        int total = userOrderApplyRepository.total(id, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = userOrderApplyRepository.pagination(id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderApply oa : list) {
                switch (OrderType.valueOf(oa.getOtype())) {
                    case PURCHASE_PURCHASE_ORDER:
                    case PURCHASE_RETURN_ORDER: {
                        TPurchaseOrder o = purchaseOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getPayPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                        HashMap<String, Object> datas = purchaseOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case STORAGE_PURCHASE_ORDER:
                    case STORAGE_DISPATCH_ORDER:
                    case STORAGE_LOSS_ORDER:
                    case STORAGE_RETURN_ORDER:
                    case STORAGE_OFFLINE_ORDER:
                    case STORAGE_BACK_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                        HashMap<String, Object> datas = storageOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case PRODUCT_COLLECT_ORDER: {
                        TProductOrder o = productOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), null);
                        HashMap<String, Object> datas = productOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case AGREEMENT_SHIPPED_ORDER:
                    case AGREEMENT_RETURN_ORDER:
                    case AGREEMENT_AGAIN_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getAid(), o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                        HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case SALE_AFTER_ORDER:
                    case SALE_LOSS_ORDER: {
                        TSaleOrder o = saleOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, null, o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getPayPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), null);
                        HashMap<String, Object> datas = saleOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getMyCheck(int id, int page, int limit, String search) {
        int total = userOrderReviewRepository.total(id, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = userOrderReviewRepository.pagination(id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderReview or : list) {
                switch (OrderType.valueOf(or.getOtype())) {
                    case PURCHASE_PURCHASE_ORDER:
                    case PURCHASE_RETURN_ORDER: {
                        TPurchaseOrder o = purchaseOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getPayPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                        HashMap<String, Object> datas = purchaseOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case STORAGE_PURCHASE_ORDER:
                    case STORAGE_DISPATCH_ORDER:
                    case STORAGE_LOSS_ORDER:
                    case STORAGE_RETURN_ORDER:
                    case STORAGE_OFFLINE_ORDER:
                    case STORAGE_BACK_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                        HashMap<String, Object> datas = storageOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case PRODUCT_COLLECT_ORDER: {
                        TProductOrder o = productOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), null);
                        HashMap<String, Object> datas = productOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case AGREEMENT_SHIPPED_ORDER:
                    case AGREEMENT_RETURN_ORDER:
                    case AGREEMENT_AGAIN_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getAid(), o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                        HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case SALE_AFTER_ORDER:
                    case SALE_LOSS_ORDER: {
                        TSaleOrder o = saleOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, null, o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getPayPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), null);
                        HashMap<String, Object> datas = saleOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getMyComplete(int id, int page, int limit, String search) {
        // TODO 根据id查公司，根据权限查具体数据
        int total = userOrderCompleteRepository.total(id, id, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = userOrderCompleteRepository.pagination(id, id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderComplete oc : list) {
                switch (OrderType.valueOf(oc.getOtype())) {
                    case PURCHASE_PURCHASE_ORDER:
                    case PURCHASE_RETURN_ORDER: {
                        TPurchaseOrder o = purchaseOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getPayPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                        HashMap<String, Object> datas = purchaseOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case STORAGE_PURCHASE_ORDER:
                    case STORAGE_DISPATCH_ORDER:
                    case STORAGE_LOSS_ORDER:
                    case STORAGE_RETURN_ORDER:
                    case STORAGE_OFFLINE_ORDER:
                    case STORAGE_BACK_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                        HashMap<String, Object> datas = storageOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case PRODUCT_COLLECT_ORDER: {
                        TProductOrder o = productOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), null);
                        HashMap<String, Object> datas = productOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case AGREEMENT_SHIPPED_ORDER:
                    case AGREEMENT_RETURN_ORDER:
                    case AGREEMENT_AGAIN_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getAid(), o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), null, o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), o.getComplete());
                        HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case SALE_AFTER_ORDER:
                    case SALE_LOSS_ORDER: {
                        TSaleOrder o = saleOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, null, o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getPayPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()), null);
                        HashMap<String, Object> datas = saleOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                            ret.put("fares", datas.get("fares"));
                            ret.put("total", datas.get("total"));
                            ret.put("remarks", datas.get("remarks"));
                        }
                        list2.add(ret);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getOrder(int id, int type, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        switch (OrderType.valueOf(type)) {
            case PURCHASE_PURCHASE_ORDER:
            case PURCHASE_RETURN_ORDER:
                val purchaseOrder = purchaseOrderRepository.find(oid);
                if (null != purchaseOrder && purchaseOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(purchaseOrder);
                }
                break;
            case STORAGE_PURCHASE_ORDER:
            case STORAGE_DISPATCH_ORDER:
            case STORAGE_LOSS_ORDER:
            case STORAGE_RETURN_ORDER:
            case STORAGE_OFFLINE_ORDER:
            case STORAGE_BACK_ORDER:
                val storageOrder = storageOrderRepository.find(oid);
                if (null != storageOrder && storageOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(storageOrder);
                }
                break;
            case PRODUCT_COLLECT_ORDER:
                val productOrder = productOrderRepository.find(oid);
                if (null != productOrder && productOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(productOrder);
                }
                break;
            case AGREEMENT_SHIPPED_ORDER:
            case AGREEMENT_RETURN_ORDER:
            case AGREEMENT_AGAIN_ORDER:
                val agreementOrder = agreementOrderRepository.find(oid);
                if (null != agreementOrder && agreementOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(agreementOrder);
                }
                break;
            case SALE_AFTER_ORDER:
            case SALE_LOSS_ORDER:
                val saleOrder = saleOrderRepository.find(oid);
                if (null != saleOrder && saleOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(saleOrder);
                }
                break;
            default:
                break;
        }
        return RestResult.fail("未查询到订单信息");
    }

    private HashMap<String, Object> createOrder(int type, int id, String batch, int sid, Integer aid, Integer rid, int unit, int curUnit, BigDecimal price,
                                                BigDecimal curPrice, BigDecimal pay, int apply, String applyTime, Integer review, String reviewTime, Byte complete) {
        val ret = new HashMap<String, Object>();
        ret.put("type", type);
        ret.put("id", id);
        ret.put("batch", batch);
        ret.put("unit", unit);
        ret.put("curUnit", curUnit);
        ret.put("price", price);
        ret.put("curPrice", curPrice);
        ret.put("pay", pay);
        ret.put("rid", rid);
        ret.put("complete", complete);

        // 获取关联订单信息
        switch (OrderType.valueOf(type)) {
            case PURCHASE_RETURN_ORDER:
            case STORAGE_RETURN_ORDER: // 采购单
                TPurchaseOrder purchase = purchaseOrderRepository.find(rid);
                if (null != purchase) {
                    ret.put("obatch", purchase.getBatch());
                }
                break;
            case AGREEMENT_RETURN_ORDER:
            case AGREEMENT_AGAIN_ORDER: // 履约单
                TAgreementOrder agreement = agreementOrderRepository.find(rid);
                if (null != agreement) {
                    ret.put("obatch", agreement.getBatch());
                }
                break;
            default:
                break;
        }

        // 获取平台账号信息
        if (null != aid) {
            TMarketAccount account = marketAccountRepository.find(aid);
            if (null != account) {
                ret.put("maccount", account.getAccount());
            }
        }

        // 获取仓库信息
        TStorage s = storageRepository.find(sid);
        if (null != s) {
            ret.put("sid", s.getId());
            ret.put("sname", s.getName());
        }

        TUser ua = userRepository.find(apply);
        if (null != ua) {
            ret.put("apply", ua.getId());
            ret.put("applyName", ua.getName());
        }
        ret.put("applyTime", applyTime);

        if (null != review) {
            TUser uv = userRepository.find(review);
            if (null != uv) {
                ret.put("review", uv.getId());
                ret.put("reviewName", uv.getName());
            }
            ret.put("reviewTime", reviewTime);
        }
        return ret;
    }
}
