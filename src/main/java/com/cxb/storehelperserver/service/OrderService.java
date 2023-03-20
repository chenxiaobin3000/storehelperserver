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
    private SaleOrderService saleOrderService;

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
    private SaleOrderRepository saleOrderRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private CloudRepository cloudRepository;

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
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = agreementOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TAgreementOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getCid(), o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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

    public RestResult getCloudOrder(int id, int type, int page, int limit, ReviewType review, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = cloudOrderRepository.total(group.getGid(), type, review, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = cloudOrderRepository.pagination(group.getGid(), type, page, limit, review, search);
        if (null != list && !list.isEmpty()) {
            for (TCloudOrder o : list) {
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                val ret = createOrder(o.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, null, o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case PURCHASE_RETURN_ORDER:
                    case PURCHASE_PURCHASE2_ORDER:
                    case PURCHASE_RETURN2_ORDER: {
                        TPurchaseOrder o = purchaseOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case STORAGE_PURCHASE2_ORDER:
                    case STORAGE_LOSS_ORDER:
                    case STORAGE_RETURN_ORDER:
                    case STORAGE_AGREEMENT_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getCid(), o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case CLOUD_LOSS_ORDER:
                    case CLOUD_BACK_ORDER:
                    case CLOUD_AGREEMENT_ORDER: {
                        TCloudOrder o = cloudOrderRepository.find(oa.getOid());
                        log.info("" + oa + "," + o);
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getCid(), o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case SALE_RETURN_ORDER: {
                        TSaleOrder o = saleOrderRepository.find(oa.getOid());
                        val ret = createOrder(oa.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, null, o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case PURCHASE_RETURN_ORDER:
                    case PURCHASE_PURCHASE2_ORDER:
                    case PURCHASE_RETURN2_ORDER: {
                        TPurchaseOrder o = purchaseOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case STORAGE_PURCHASE2_ORDER:
                    case STORAGE_LOSS_ORDER:
                    case STORAGE_RETURN_ORDER:
                    case STORAGE_AGREEMENT_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getCid(), o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case CLOUD_LOSS_ORDER:
                    case CLOUD_BACK_ORDER:
                    case CLOUD_AGREEMENT_ORDER: {
                        TCloudOrder o = cloudOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getCid(), o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case SALE_RETURN_ORDER: {
                        TSaleOrder o = saleOrderRepository.find(or.getOid());
                        val ret = createOrder(or.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, null, o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case PURCHASE_RETURN_ORDER:
                    case PURCHASE_PURCHASE2_ORDER:
                    case PURCHASE_RETURN2_ORDER: {
                        TPurchaseOrder o = purchaseOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case STORAGE_PURCHASE2_ORDER:
                    case STORAGE_LOSS_ORDER:
                    case STORAGE_RETURN_ORDER:
                    case STORAGE_AGREEMENT_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, o.getPid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getCid(), o.getRid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case CLOUD_LOSS_ORDER:
                    case CLOUD_BACK_ORDER:
                    case CLOUD_AGREEMENT_ORDER: {
                        TCloudOrder o = cloudOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), o.getCid(), o.getOid(), o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
                    case SALE_RETURN_ORDER: {
                        TSaleOrder o = saleOrderRepository.find(oc.getOid());
                        val ret = createOrder(oc.getOtype(), o.getId(), o.getBatch(), o.getSid(), null, null, o.getUnit(), o.getCurUnit(), o.getPrice(), o.getCurPrice(), o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
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
            case PURCHASE_PURCHASE2_ORDER:
            case PURCHASE_RETURN2_ORDER:
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
            case STORAGE_AGREEMENT_ORDER:
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
            case CLOUD_LOSS_ORDER:
            case CLOUD_BACK_ORDER:
            case CLOUD_AGREEMENT_ORDER:
                val cloudOrder = cloudOrderRepository.find(oid);
                if (null != cloudOrder && cloudOrder.getGid().equals(group.getGid())) {
                    return RestResult.ok(cloudOrder);
                }
                break;
            case SALE_RETURN_ORDER:
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

    private HashMap<String, Object> createOrder(int type, int id, String batch, int sid, Integer cid, Integer rid, int unit, int curUnit, BigDecimal price, BigDecimal curPrice, int apply, String applyTime, Integer review, String reviewTime) {
        val ret = new HashMap<String, Object>();
        ret.put("type", type);
        ret.put("id", id);
        ret.put("batch", batch);
        ret.put("unit", unit);
        ret.put("curUnit", curUnit);
        ret.put("price", price);
        ret.put("curPrice", curPrice);
        ret.put("rid", rid);

        // 获取关联订单信息
        switch (OrderType.valueOf(type)) {
            case PURCHASE_RETURN_ORDER:
            case PURCHASE_RETURN2_ORDER:
            case STORAGE_RETURN_ORDER:
            case CLOUD_PURCHASE_ORDER:
            case CLOUD_RETURN_ORDER: // 采购单
                TPurchaseOrder purchase = purchaseOrderRepository.find(rid);
                if (null != purchase) {
                    ret.put("obatch", purchase.getBatch());
                }
                break;
            case STORAGE_PURCHASE2_ORDER: // 调度单
                TStorageOrder storage = storageOrderRepository.find(rid);
                if (null != storage) {
                    ret.put("obatch", storage.getBatch());
                }
                break;
            case PRODUCT_COMPLETE_ORDER: // 生产单
                TProductOrder product = productOrderRepository.find(rid);
                if (null != product) {
                    ret.put("obatch", product.getBatch());
                }
                break;
            case STORAGE_AGREEMENT_ORDER:
            case AGREEMENT_RETURN_ORDER:
            case CLOUD_AGREEMENT_ORDER:
            case CLOUD_BACK_ORDER: // 履约单
                TAgreementOrder agreement = agreementOrderRepository.find(rid);
                if (null != agreement) {
                    ret.put("obatch", agreement.getBatch());
                }
                break;
            default:
                break;
        }

        // 获取仓库信息
        switch (OrderType.valueOf(type)) {
            case PURCHASE_PURCHASE2_ORDER:
            case PURCHASE_RETURN2_ORDER: {
                TCloud c = cloudRepository.find(sid);
                if (null != c) {
                    ret.put("sid", c.getId());
                    ret.put("sname", c.getName());
                }
                break;
            }
            case CLOUD_PURCHASE_ORDER:
            case CLOUD_RETURN_ORDER:
            case CLOUD_LOSS_ORDER:
            case CLOUD_BACK_ORDER:
            case CLOUD_AGREEMENT_ORDER: {
                log.info("sid:" + sid + ",cid:" + cid + ",type:" + type);
                TCloud c = cloudRepository.find(sid);
                if (null != c) {
                    ret.put("sid", c.getId());
                    ret.put("sname", c.getName());
                }
                break;
            }
            default: {
                TStorage s = storageRepository.find(sid);
                if (null != s) {
                    ret.put("sid", s.getId());
                    ret.put("sname", s.getName());
                }
                break;
            }
        }

        // 云仓
        if (null != cid) {
            TCloud c = cloudRepository.find(cid);
            if (null != c) {
                ret.put("cid", c.getId());
                ret.put("cname", c.getName());
            }
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
