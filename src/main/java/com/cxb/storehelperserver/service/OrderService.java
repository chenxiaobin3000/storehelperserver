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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.BusinessType;
import static com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.*;
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
    private OfflineOrderService offlineOrderService;

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
    private AgreementRemarkRepository agreementRemarkRepository;

    @Resource
    private AgreementReturnRepository agreementReturnRepository;

    @Resource
    private AgreementStorageRepository agreementStorageRepository;

    @Resource
    private OfflineOrderRepository offlineOrderRepository;

    @Resource
    private OfflineRemarkRepository offlineRemarkRepository;

    @Resource
    private OfflineReturnRepository offlineReturnRepository;

    @Resource
    private OfflineStorageRepository offlineStorageRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductRemarkRepository productRemarkRepository;

    @Resource
    private ProductAgreementRepository productAgreementRepository;

    @Resource
    private ProductCompleteRepository productCompleteRepository;

    @Resource
    private ProductOfflineRepository productOfflineRepository;

    @Resource
    private ProductStorageRepository productStorageRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseRemarkRepository purchaseRemarkRepository;

    @Resource
    private PurchaseReturnRepository purchaseReturnRepository;

    @Resource
    private PurchaseStorageRepository purchaseStorageRepository;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageRemarkRepository storageRemarkRepository;

    @Resource
    private SaleOrderRepository saleOrderRepository;

    @Resource
    private SaleRemarkRepository saleRemarkRepository;

    @Resource
    private SaleTypeRepository saleTypeRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private SupplierRepository supplierRepository;

    @Resource
    private MarketAccountRepository marketAccountRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

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
            case BUSINESS_OFFLINE: {
                // 验证公司
                TOfflineOrder order = offlineOrderRepository.find(oid);
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
                offlineOrderService.clean(oid);

                // 备注
                if (null != remark && remark.length() > 0) {
                    if (!offlineRemarkRepository.insert(oid, remark, new Date())) {
                        return RestResult.fail("添加备注失败");
                    }
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
            case BUSINESS_OFFLINE: {
                // 验证公司
                TOfflineOrder order = offlineOrderRepository.find(oid);
                if (null == order) {
                    return RestResult.fail("未查询到订单信息");
                }
                String msg = checkService.checkGroup(id, order.getGid());
                if (null != msg) {
                    return RestResult.fail(msg);
                }
                offlineOrderService.clean(oid);
                if (!order.getReview().equals(rid)) {
                    RestResult.fail("要删除备注，请联系订单审核人");
                }
                if (!offlineRemarkRepository.delete(rid)) {
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

    public RestResult getAgreementOrder(int id, int aid, int type, int page, int limit, ReviewType review, CompleteType complete, String date, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = agreementOrderService.total(group.getGid(), aid, type, review, complete, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = agreementOrderService.pagination(group.getGid(), aid, type, page, limit, review, complete, date, search);
        val list2 = new ArrayList<HashMap<String, Object>>();
        if (null != list && !list.isEmpty()) {
            for (TAgreementOrder o : list) {
                val ret = createAgreementOrder(o);
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getOfflineOrder(int id, int aid, int type, int page, int limit, ReviewType review, CompleteType complete, String date, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = offlineOrderService.total(group.getGid(), aid, type, review, complete, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = offlineOrderService.pagination(group.getGid(), aid, type, page, limit, review, complete, date, search);
        val list2 = new ArrayList<HashMap<String, Object>>();
        if (null != list && !list.isEmpty()) {
            for (TOfflineOrder o : list) {
                val ret = createOfflineOrder(o);
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getProductOrder(int id, int type, int page, int limit, ReviewType review, String date, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = productOrderService.total(group.getGid(), type, review, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = productOrderService.pagination(group.getGid(), type, page, limit, review, date, search);
        val list2 = new ArrayList<HashMap<String, Object>>();
        if (null != list && !list.isEmpty()) {
            for (TProductOrder o : list) {
                val ret = createProductOrder(o);
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getPurchaseOrder(int id, int type, int page, int limit, ReviewType review, CompleteType complete, String date, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = purchaseOrderService.total(group.getGid(), type, review, complete, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = purchaseOrderService.pagination(group.getGid(), type, page, limit, review, complete, date, search);
        val list2 = new ArrayList<HashMap<String, Object>>();
        if (null != list && !list.isEmpty()) {
            for (TPurchaseOrder o : list) {
                val ret = createPurchaseOrder(o);
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getStorageOrder(int id, int type, int page, int limit, ReviewType review, String date, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = storageOrderService.total(group.getGid(), type, review, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        List<TStorageOrder> list = storageOrderService.pagination(group.getGid(), type, page, limit, review, date, search);
        val list2 = new ArrayList<HashMap<String, Object>>();
        if (null != list && !list.isEmpty()) {
            for (TStorageOrder o : list) {
                val ret = createStorageOrder(o);
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getSaleOrder(int id, int type, int page, int limit, ReviewType review, String date, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = saleOrderService.total(group.getGid(), type, review, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = saleOrderService.pagination(group.getGid(), type, page, limit, review, date, search);
        val list2 = new ArrayList<HashMap<String, Object>>();
        if (null != list && !list.isEmpty()) {
            for (TSaleOrder o : list) {
                val ret = createSaleOrder(o);
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
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = userOrderApplyRepository.pagination(id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderApply oa : list) {
                val ret = createOrder(oa.getOtype(), oa.getOid());
                list2.add(ret);
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
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = userOrderReviewRepository.pagination(id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderReview or : list) {
                val ret = createOrder(or.getOtype(), or.getOid());
                list2.add(ret);
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
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = userOrderCompleteRepository.pagination(id, id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderComplete oc : list) {
                val ret = createOrder(oc.getOtype(), oc.getOid());
                list2.add(ret);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public  HashMap<String, Object> createOrder(int type, int oid) {
        switch (OrderType.valueOf(type)) {
            case PURCHASE_PURCHASE_ORDER:
            case PURCHASE_RETURN_ORDER: {
                TPurchaseOrder o = purchaseOrderRepository.find(oid);
                return createPurchaseOrder(o);
            }
            case STORAGE_PURCHASE_IN_ORDER:
            case STORAGE_PURCHASE_OUT_ORDER:
            case STORAGE_PRODUCT_IN_ORDER:
            case STORAGE_PRODUCT_OUT_ORDER:
            case STORAGE_AGREEMENT_IN_ORDER:
            case STORAGE_AGREEMENT_OUT_ORDER:
            case STORAGE_OFFLINE_IN_ORDER:
            case STORAGE_OFFLINE_OUT_ORDER:
            case STORAGE_DISPATCH_IN_ORDER:
            case STORAGE_DISPATCH_OUT_ORDER:
            case STORAGE_LOSS_ORDER: {
                TStorageOrder o = storageOrderRepository.find(oid);
                return createStorageOrder(o);
            }
            case PRODUCT_PROCESS_ORDER:
            case PRODUCT_COMPLETE_ORDER:
            case PRODUCT_LOSS_ORDER: {
                TProductOrder o = productOrderRepository.find(oid);
                return createProductOrder(o);
            }
            case AGREEMENT_SHIPPED_ORDER:
            case AGREEMENT_RETURN_ORDER: {
                TAgreementOrder o = agreementOrderRepository.find(oid);
                return createAgreementOrder(o);
            }
            case SALE_SALE_ORDER:
            case SALE_LOSS_ORDER: {
                TSaleOrder o = saleOrderRepository.find(oid);
                return createSaleOrder(o);
            }
            case OFFLINE_OFFLINE_ORDER:
            case OFFLINE_RETURN_ORDER: {
                TOfflineOrder o = offlineOrderRepository.find(oid);
                return createOfflineOrder(o);
            }
            default:
                break;
        }
        return null;
    }

    private HashMap<String, Object> createAgreementOrder(TAgreementOrder order) {
        val ret = new HashMap<String, Object>();
        ret.put("type", order.getOtype());
        ret.put("id", order.getId());
        ret.put("batch", order.getBatch());
        ret.put("price", order.getPrice());
        ret.put("curPrice", order.getCurPrice());
        ret.put("value", order.getValue());
        ret.put("curValue", order.getCurValue());
        ret.put("complete", order.getComplete());

        HashMap<String, Object> datas = agreementOrderService.find(order.getId());
        if (null != datas) {
            ret.put("comms", datas.get("comms"));
            ret.put("attrs", datas.get("attrs"));
            ret.put("fares", datas.get("fares"));
            ret.put("total", datas.get("total"));
            ret.put("remarks", datas.get("remarks"));
        }

        TMarketAccount account = marketAccountRepository.find(order.getAid());
        if (null != account) {
            ret.put("maccount", account.getAccount());
            ret.put("mremark", account.getRemark());
        }

        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        TUser ua = userRepository.find(order.getApply());
        if (null != ua) {
            ret.put("apply", ua.getId());
            ret.put("applyName", ua.getName());
        }
        ret.put("applyTime", dateFormat.format(order.getApplyTime()));

        Integer review = order.getReview();
        if (null != review) {
            TUser uv = userRepository.find(review);
            if (null != uv) {
                ret.put("review", uv.getId());
                ret.put("reviewName", uv.getName());
            }
            ret.put("reviewTime", dateFormat.format(order.getReviewTime()));
        }

        if (order.getOtype().equals(AGREEMENT_RETURN_ORDER.getValue())) {
            TAgreementReturn agreementReturn = agreementReturnRepository.find(order.getId());
            if (null != agreementReturn) {
                TAgreementOrder agreement = agreementOrderRepository.find(agreementReturn.getAid());
                if (null != agreement) {
                    ret.put("obatch", agreement.getBatch());
                }
            }
        }

        // 查询关联仓储单据
        TAgreementStorage as = agreementStorageRepository.find(order.getId());
        if (null != as) {
            TStorageOrder so = storageOrderRepository.find(as.getSid());
            if (null != so) {
                createSubOrder(so, ret);
            }
        }
        return ret;
    }

    private HashMap<String, Object> createOfflineOrder(TOfflineOrder order) {
        val ret = new HashMap<String, Object>();
        ret.put("type", order.getOtype());
        ret.put("id", order.getId());
        ret.put("batch", order.getBatch());
        ret.put("price", order.getPrice());
        ret.put("curPrice", order.getCurPrice());
        ret.put("pay", order.getPayPrice());
        ret.put("value", order.getValue());
        ret.put("curValue", order.getCurValue());
        ret.put("complete", order.getComplete());

        HashMap<String, Object> datas = offlineOrderService.find(order.getId());
        if (null != datas) {
            ret.put("comms", datas.get("comms"));
            ret.put("attrs", datas.get("attrs"));
            ret.put("fares", datas.get("fares"));
            ret.put("total", datas.get("total"));
            ret.put("remarks", datas.get("remarks"));
        }

        TMarketAccount account = marketAccountRepository.find(order.getAid());
        if (null != account) {
            ret.put("maccount", account.getAccount());
            ret.put("mremark", account.getRemark());
        }

        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        TUser ua = userRepository.find(order.getApply());
        if (null != ua) {
            ret.put("apply", ua.getId());
            ret.put("applyName", ua.getName());
        }
        ret.put("applyTime", dateFormat.format(order.getApplyTime()));

        Integer review = order.getReview();
        if (null != review) {
            TUser uv = userRepository.find(review);
            if (null != uv) {
                ret.put("review", uv.getId());
                ret.put("reviewName", uv.getName());
            }
            ret.put("reviewTime", dateFormat.format(order.getReviewTime()));
        }

        if (order.getOtype().equals(OFFLINE_RETURN_ORDER.getValue())) {
            TOfflineReturn offlineReturn = offlineReturnRepository.find(order.getId());
            if (null != offlineReturn) {
                TOfflineOrder offline = offlineOrderRepository.find(offlineReturn.getSid());
                if (null != offline) {
                    ret.put("obatch", offline.getBatch());
                }
            }
        }

        // 查询关联仓储单据
        TOfflineStorage os = offlineStorageRepository.find(order.getId());
        if (null != os) {
            TStorageOrder so = storageOrderRepository.find(os.getSid());
            if (null != so) {
                createSubOrder(so, ret);
            }
        }
        return ret;
    }

    private HashMap<String, Object> createProductOrder(TProductOrder order) {
        val ret = new HashMap<String, Object>();
        ret.put("type", order.getOtype());
        ret.put("id", order.getId());
        ret.put("batch", order.getBatch());
        ret.put("price", order.getPrice());
        ret.put("curPrice", order.getCurPrice());
        ret.put("unit", order.getUnit());
        ret.put("curUnit", order.getCurUnit());
        ret.put("complete", order.getComplete());

        HashMap<String, Object> datas = productOrderService.find(order.getId());
        if (null != datas) {
            ret.put("comms", datas.get("comms"));
            ret.put("attrs", datas.get("attrs"));
            ret.put("total", datas.get("total"));
            ret.put("remarks", datas.get("remarks"));
        }

        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        TUser ua = userRepository.find(order.getApply());
        if (null != ua) {
            ret.put("apply", ua.getId());
            ret.put("applyName", ua.getName());
        }
        ret.put("applyTime", dateFormat.format(order.getApplyTime()));

        Integer review = order.getReview();
        if (null != review) {
            TUser uv = userRepository.find(review);
            if (null != uv) {
                ret.put("review", uv.getId());
                ret.put("reviewName", uv.getName());
            }
            ret.put("reviewTime", dateFormat.format(order.getReviewTime()));
        }

        if (order.getOtype().equals(PRODUCT_COMPLETE_ORDER.getValue())) {
            TProductComplete productComplete = productCompleteRepository.find(order.getId());
            if (null != productComplete) {
                TProductOrder product = productOrderRepository.find(productComplete.getPid());
                if (null != product) {
                    ret.put("obatch", product.getBatch());
                }
            }
        }

        // 查询关联仓储单据
        TProductStorage ps = productStorageRepository.find(order.getId());
        if (null != ps) {
            TStorageOrder so = storageOrderRepository.find(ps.getSid());
            if (null != so) {
                createSubOrder(so, ret);
            }
        }
        return ret;
    }

    private HashMap<String, Object> createPurchaseOrder(TPurchaseOrder order) {
        val ret = new HashMap<String, Object>();
        ret.put("type", order.getOtype());
        ret.put("id", order.getId());
        ret.put("batch", order.getBatch());
        ret.put("price", order.getPrice());
        ret.put("curPrice", order.getCurPrice());
        ret.put("pay", order.getPayPrice());
        ret.put("unit", order.getUnit());
        ret.put("curUnit", order.getCurUnit());
        ret.put("complete", order.getComplete());

        HashMap<String, Object> datas = purchaseOrderService.find(order.getId());
        if (null != datas) {
            ret.put("comms", datas.get("comms"));
            ret.put("attrs", datas.get("attrs"));
            ret.put("fares", datas.get("fares"));
            ret.put("total", datas.get("total"));
            ret.put("remarks", datas.get("remarks"));
        }

        // 供货商
        int sid = order.getSupplier();
        if (0 != sid) {
            TSupplier supplier = supplierRepository.find(sid);
            if (null != supplier) {
                ret.put("supplier", supplier.getName());
            }
        }

        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        TUser ua = userRepository.find(order.getApply());
        if (null != ua) {
            ret.put("apply", ua.getId());
            ret.put("applyName", ua.getName());
        }
        ret.put("applyTime", dateFormat.format(order.getApplyTime()));

        Integer review = order.getReview();
        if (null != review) {
            TUser uv = userRepository.find(review);
            if (null != uv) {
                ret.put("review", uv.getId());
                ret.put("reviewName", uv.getName());
            }
            ret.put("reviewTime", dateFormat.format(order.getReviewTime()));
        }

        if (order.getOtype().equals(PURCHASE_RETURN_ORDER.getValue())) {
            TPurchaseReturn purchaseReturn = purchaseReturnRepository.find(order.getId());
            if (null != purchaseReturn) {
                TPurchaseOrder purchase = purchaseOrderRepository.find(purchaseReturn.getPid());
                if (null != purchase) {
                    ret.put("obatch", purchase.getBatch());
                }
            }
        }

        // 查询关联仓储单据
        TPurchaseStorage ps = purchaseStorageRepository.find(order.getId());
        if (null != ps) {
            TStorageOrder so = storageOrderRepository.find(ps.getSid());
            if (null != so) {
                createSubOrder(so, ret);
            }
        }
        return ret;
    }

    private HashMap<String, Object> createSaleOrder(TSaleOrder order) {
        val ret = new HashMap<String, Object>();
        ret.put("type", order.getOtype());
        ret.put("id", order.getId());
        ret.put("batch", order.getBatch());
        ret.put("price", order.getPrice());
        ret.put("value", order.getValue());
        ret.put("pay", order.getPayPrice());
        ret.put("fine", order.getFine());
        val saleType = saleTypeRepository.find(order.getTid());
        if (null != saleType) {
            ret.put("stype", saleType.getName());
        }

        HashMap<String, Object> datas = saleOrderService.find(order.getId());
        if (null != datas) {
            ret.put("comms", datas.get("comms"));
            ret.put("attrs", datas.get("attrs"));
            ret.put("total", datas.get("total"));
            ret.put("remarks", datas.get("remarks"));
        }

        TMarketAccount account = marketAccountRepository.find(order.getAid());
        if (null != account) {
            ret.put("maccount", account.getAccount());
            ret.put("mremark", account.getRemark());
        }

        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        TUser ua = userRepository.find(order.getApply());
        if (null != ua) {
            ret.put("apply", ua.getId());
            ret.put("applyName", ua.getName());
        }
        ret.put("applyTime", dateFormat.format(order.getApplyTime()));

        Integer review = order.getReview();
        if (null != review) {
            TUser uv = userRepository.find(review);
            if (null != uv) {
                ret.put("review", uv.getId());
                ret.put("reviewName", uv.getName());
            }
            ret.put("reviewTime", dateFormat.format(order.getReviewTime()));
        }

        return ret;
    }

    private HashMap<String, Object> createStorageOrder(TStorageOrder order) {
        val ret = new HashMap<String, Object>();
        ret.put("type", order.getOtype());
        ret.put("id", order.getId());
        ret.put("batch", order.getBatch());
        ret.put("price", order.getPrice());
        ret.put("unit", order.getUnit());
        ret.put("value", order.getValue());

        HashMap<String, Object> datas = storageOrderService.find(order.getId());
        if (null != datas) {
            ret.put("comms", datas.get("comms"));
            ret.put("attrs", datas.get("attrs"));
            ret.put("fares", datas.get("fares"));
            ret.put("total", datas.get("total"));
            ret.put("remarks", datas.get("remarks"));
        }

        TStorage s = storageRepository.find(order.getSid());
        if (null != s) {
            ret.put("sid", s.getId());
            ret.put("sname", s.getName());
        }

        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        TUser ua = userRepository.find(order.getApply());
        if (null != ua) {
            ret.put("apply", ua.getId());
            ret.put("applyName", ua.getName());
        }
        ret.put("applyTime", dateFormat.format(order.getApplyTime()));

        Integer review = order.getReview();
        if (null != review) {
            TUser uv = userRepository.find(review);
            if (null != uv) {
                ret.put("review", uv.getId());
                ret.put("reviewName", uv.getName());
            }
            ret.put("reviewTime", dateFormat.format(order.getReviewTime()));
        }

        switch (OrderType.valueOf(order.getOtype())) {
            case STORAGE_PURCHASE_IN_ORDER:
            case STORAGE_PURCHASE_OUT_ORDER:
                TPurchaseReturn purchaseReturn = purchaseReturnRepository.find(order.getId());
                if (null != purchaseReturn) {
                    TPurchaseOrder purchase = purchaseOrderRepository.find(purchaseReturn.getPid());
                    if (null != purchase) {
                        ret.put("obatch", purchase.getBatch());
                    }
                }
                break;
            case STORAGE_PRODUCT_IN_ORDER:
            case STORAGE_PRODUCT_OUT_ORDER:
                TProductComplete productComplete = productCompleteRepository.find(order.getId());
                if (null != productComplete) {
                    TProductOrder product = productOrderRepository.find(productComplete.getPid());
                    if (null != product) {
                        ret.put("obatch", product.getBatch());
                    }
                }
                break;
            case STORAGE_AGREEMENT_IN_ORDER:
            case STORAGE_AGREEMENT_OUT_ORDER:
                TAgreementReturn agreementReturn = agreementReturnRepository.find(order.getId());
                if (null != agreementReturn) {
                    TAgreementOrder agreement = agreementOrderRepository.find(agreementReturn.getAid());
                    if (null != agreement) {
                        ret.put("obatch", agreement.getBatch());
                    }
                }
                break;
            case STORAGE_OFFLINE_IN_ORDER:
            case STORAGE_OFFLINE_OUT_ORDER:
                TOfflineReturn offlineReturn = offlineReturnRepository.find(order.getId());
                if (null != offlineReturn) {
                    TOfflineOrder offline = offlineOrderRepository.find(offlineReturn.getSid());
                    if (null != offline) {
                        ret.put("obatch", offline.getBatch());
                    }
                }
                break;
            default:
                break;
        }
        return ret;
    }

    private void createSubOrder(TStorageOrder order, HashMap<String, Object> data) {
        data.put("ssid", order.getId());
        data.put("sbatch", order.getBatch());

        TStorage s = storageRepository.find(order.getSid());
        if (null != s) {
            data.put("sid", s.getId());
            data.put("sname", s.getName());
        }
    }
}
