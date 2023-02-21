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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static com.cxb.storehelperserver.util.TypeDefine.OrderType;

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
    private StorageOrderService storageOrderService;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

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

    public RestResult getAgreementOrder(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = agreementOrderRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = agreementOrderRepository.pagination(group.getGid(), page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TAgreementOrder o : list) {
                val ret = createOrder(o.getOtype() ? OrderType.AGREEMENT_OUT_ORDER : OrderType.AGREEMENT_IN_ORDER,
                        o.getId(), o.getBatch(), o.getSid(), o.getValue(), o.getApply(), dateFormat.format(o.getApplyTime()),
                        o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                HashMap<String, Object> datas = storageOrderService.find(id);
                if (null != datas) {
                    ret.put("comms", datas.get("comms"));
                    ret.put("attrs", datas.get("attrs"));
                }
                list2.add(ret);
            }
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getProductOrder(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = productOrderRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = productOrderRepository.pagination(group.getGid(), page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TProductOrder o : list) {
                val ret = createOrder(o.getOtype() ? OrderType.PRODUCT_OUT_ORDER : OrderType.PRODUCT_IN_ORDER,
                        o.getId(), o.getBatch(), o.getSid(), o.getValue(), o.getApply(), dateFormat.format(o.getApplyTime()),
                        o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                HashMap<String, Object> datas = storageOrderService.find(id);
                if (null != datas) {
                    ret.put("comms", datas.get("comms"));
                    ret.put("attrs", datas.get("attrs"));
                }
                list2.add(ret);
            }
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getStorageOrder(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = storageOrderRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = storageOrderRepository.pagination(group.getGid(), page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TStorageOrder o : list) {
                val ret = createOrder(o.getOtype() ? OrderType.STORAGE_OUT_ORDER : OrderType.STORAGE_IN_ORDER,
                        o.getId(), o.getBatch(), o.getSid(), o.getValue(), o.getApply(), dateFormat.format(o.getApplyTime()),
                        o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                HashMap<String, Object> datas = storageOrderService.find(id);
                if (null != datas) {
                    ret.put("comms", datas.get("comms"));
                    ret.put("attrs", datas.get("attrs"));
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
                    case STORAGE_IN_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oa.getOid());
                        val ret = createOrder(OrderType.STORAGE_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = storageOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case STORAGE_OUT_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oa.getOid());
                        val ret = createOrder(OrderType.STORAGE_OUT_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = storageOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case PRODUCT_IN_ORDER: {
                        TProductOrder o = productOrderRepository.find(oa.getOid());
                        val ret = createOrder(OrderType.PRODUCT_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = productOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case PRODUCT_OUT_ORDER: {
                        TProductOrder o = productOrderRepository.find(oa.getOid());
                        val ret = createOrder(OrderType.PRODUCT_OUT_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = productOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case AGREEMENT_IN_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(oa.getOid());
                        val ret = createOrder(OrderType.AGREEMENT_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case AGREEMENT_OUT_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(oa.getOid());
                        val ret = createOrder(OrderType.AGREEMENT_OUT_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
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
                    case STORAGE_IN_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(or.getOid());
                        val ret = createOrder(OrderType.STORAGE_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = storageOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case STORAGE_OUT_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(or.getOid());
                        val ret = createOrder(OrderType.STORAGE_OUT_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = storageOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case PRODUCT_IN_ORDER: {
                        TProductOrder o = productOrderRepository.find(or.getOid());
                        val ret = createOrder(OrderType.PRODUCT_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = productOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case PRODUCT_OUT_ORDER: {
                        TProductOrder o = productOrderRepository.find(or.getOid());
                        val ret = createOrder(OrderType.PRODUCT_OUT_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = productOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case AGREEMENT_IN_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(or.getOid());
                        val ret = createOrder(OrderType.AGREEMENT_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case AGREEMENT_OUT_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(or.getOid());
                        val ret = createOrder(OrderType.AGREEMENT_OUT_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
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
        // 根据id查公司，根据权限查具体数据
        int total = userOrderCompleteRepository.total(id, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = userOrderCompleteRepository.pagination(id, page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TUserOrderComplete oc : list) {
                switch (OrderType.valueOf(oc.getOtype())) {
                    case STORAGE_IN_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oc.getOid());
                        val ret = createOrder(OrderType.STORAGE_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = storageOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case STORAGE_OUT_ORDER: {
                        TStorageOrder o = storageOrderRepository.find(oc.getOid());
                        val ret = createOrder(OrderType.STORAGE_OUT_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = storageOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case PRODUCT_IN_ORDER: {
                        TProductOrder o = productOrderRepository.find(oc.getOid());
                        val ret = createOrder(OrderType.PRODUCT_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = productOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case PRODUCT_OUT_ORDER: {
                        TProductOrder o = productOrderRepository.find(oc.getOid());
                        val ret = createOrder(OrderType.PRODUCT_OUT_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = productOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case AGREEMENT_IN_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(oc.getOid());
                        val ret = createOrder(OrderType.AGREEMENT_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
                        }
                        list2.add(ret);
                        break;
                    }
                    case AGREEMENT_OUT_ORDER: {
                        TAgreementOrder o = agreementOrderRepository.find(oc.getOid());
                        val ret = createOrder(OrderType.AGREEMENT_OUT_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()), o.getReview(),
                                null == o.getReview() ? null : dateFormat.format(o.getReviewTime()));
                        HashMap<String, Object> datas = agreementOrderService.find(o.getId());
                        if (null != datas) {
                            ret.put("comms", datas.get("comms"));
                            ret.put("attrs", datas.get("attrs"));
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

    private HashMap<String, Object> createOrder(OrderType type, int id, String batch, int sid, int value,
                                                int apply, String applyTime, Integer review, String reviewTime) {
        val ret = new HashMap<String, Object>();
        ret.put("type", type.getValue());
        ret.put("id", id);
        ret.put("batch", batch);
        ret.put("value", value);

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
