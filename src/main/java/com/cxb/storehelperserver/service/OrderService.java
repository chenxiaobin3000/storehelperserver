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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

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
    private CloudOrderService cloudOrderService;

    @Resource
    private ProductOrderService productOrderService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private StorageOrderService storageOrderService;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private CloudOrderRepository cloudOrderRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult getAgreementOrder(int id, int type, int page, int limit, ReviewType review, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = agreementOrderRepository.total(group.getGid(), type, review, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = agreementOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TAgreementOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getRid(), o.getPrice(), o.getApply(),
                        dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getCloudOrder(int id, int type, int page, int limit, ReviewType review, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = cloudOrderRepository.total(group.getGid(), type, review, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = cloudOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TCloudOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getOid(), o.getPrice(), o.getApply(),
                        dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                HashMap<String, Object> datas = cloudOrderService.find(o.getId());
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

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getProductOrder(int id, int type, int page, int limit, ReviewType review, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = productOrderRepository.total(group.getGid(), type, review, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = productOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TProductOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPrice(), o.getApply(),
                        dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getPurchaseOrder(int id, int type, int page, int limit, ReviewType review, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = purchaseOrderRepository.total(group.getGid(), type, review, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = purchaseOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TPurchaseOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getRid(), o.getPrice(), o.getApply(),
                        dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getStorageOrder(int id, int type, int page, int limit, ReviewType review, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = storageOrderRepository.total(group.getGid(), type, review, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = storageOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TStorageOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getOid(), o.getPrice(), o.getApply(),
                        dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getMyWait(int id, int page, int limit, String search) {
        int total = userOrderApplyRepository.total(id, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = userOrderApplyRepository.pagination(id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderApply oa : list) {
                switch (OrderType.valueOf(oa.getOtype())) {
                    case PURCHASE_PURCHASE_ORDER:
                    case PURCHASE_RETURN_ORDER: {
                        TPurchaseOrder o = purchaseOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getRid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case STORAGE_PURCHASE2_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getOid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case PRODUCT_PROCESS_ORDER:
                    case PRODUCT_COMPLETE_ORDER:
                    case PRODUCT_LOSS_ORDER: {
                        TProductOrder o = productOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case AGREEMENT_RETURN_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getRid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case CLOUD_PURCHASE_ORDER:
                    case CLOUD_RETURN_ORDER:
                    case CLOUD_SALE_ORDER:
                    case CLOUD_LOSS_ORDER: {
                        TCloudOrder o = cloudOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getOid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = cloudOrderService.find(o.getId());
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

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getMyCheck(int id, int page, int limit, String search) {
        int total = userOrderReviewRepository.total(id, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = userOrderReviewRepository.pagination(id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderReview or : list) {
                switch (OrderType.valueOf(or.getOtype())) {
                    case PURCHASE_PURCHASE_ORDER:
                    case PURCHASE_RETURN_ORDER: {
                        TPurchaseOrder o = purchaseOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getRid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case STORAGE_PURCHASE2_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getOid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case PRODUCT_PROCESS_ORDER:
                    case PRODUCT_COMPLETE_ORDER:
                    case PRODUCT_LOSS_ORDER: {
                        TProductOrder o = productOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case AGREEMENT_RETURN_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getRid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case CLOUD_PURCHASE_ORDER:
                    case CLOUD_RETURN_ORDER:
                    case CLOUD_SALE_ORDER:
                    case CLOUD_LOSS_ORDER: {
                        TCloudOrder o = cloudOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getOid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = cloudOrderService.find(o.getId());
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

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getMyComplete(int id, int page, int limit, String search) {
        // TODO 根据id查公司，根据权限查具体数据
        int total = userOrderCompleteRepository.total(id, id, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = userOrderCompleteRepository.pagination(id, id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderComplete oc : list) {
                switch (OrderType.valueOf(oc.getOtype())) {
                    case PURCHASE_PURCHASE_ORDER:
                    case PURCHASE_RETURN_ORDER: {
                        TPurchaseOrder o = purchaseOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getRid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case STORAGE_PURCHASE2_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getOid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case PRODUCT_PROCESS_ORDER:
                    case PRODUCT_COMPLETE_ORDER:
                    case PRODUCT_LOSS_ORDER: {
                        TProductOrder o = productOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case AGREEMENT_RETURN_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getRid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case CLOUD_PURCHASE_ORDER:
                    case CLOUD_RETURN_ORDER:
                    case CLOUD_SALE_ORDER:
                    case CLOUD_LOSS_ORDER: {
                        TCloudOrder o = cloudOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getOid(), o.getPrice(), o.getApply(),
                                dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = cloudOrderService.find(o.getId());
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

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
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
            case STORAGE_PURCHASE2_ORDER:
            case STORAGE_LOSS_ORDER:
            case STORAGE_RETURN_ORDER:
                val storageOrder = storageOrderRepository.find(oid);
                if (null != storageOrder && storageOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(storageOrder);
                }
                break;
            case PRODUCT_PROCESS_ORDER:
            case PRODUCT_COMPLETE_ORDER:
            case PRODUCT_LOSS_ORDER:
                val productOrder = productOrderRepository.find(oid);
                if (null != productOrder && productOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(productOrder);
                }
                break;
            case AGREEMENT_SHIPPED_ORDER:
            case AGREEMENT_RETURN_ORDER:
                val agreementOrder = agreementOrderRepository.find(oid);
                if (null != agreementOrder && agreementOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(agreementOrder);
                }
                break;
            case CLOUD_PURCHASE_ORDER:
            case CLOUD_RETURN_ORDER:
            case CLOUD_SALE_ORDER:
            case CLOUD_LOSS_ORDER:
                val cloudOrder = cloudOrderRepository.find(oid);
                if (null != cloudOrder && cloudOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(cloudOrder);
                }
                break;
            default:
                break;
        }
        return RestResult.fail("未查询到订单信息");
    }

    private HashMap<String, Object> createOrder(int type, int id, String batch, int sid, Integer rid, BigDecimal price,
                                                int apply, String applyTime, Integer review, String reviewTime) {
        val ret = new HashMap<String, Object>();
        ret.put("type", type);
        ret.put("id", id);
        ret.put("batch", batch);
        ret.put("price", price);
        ret.put("rid", rid);

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
