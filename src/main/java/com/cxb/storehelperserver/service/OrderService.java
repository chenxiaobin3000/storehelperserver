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
    private StorageOrderService storageOrderService;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult getStorageOrder(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
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
                list2.add(createOrder(OrderType.STORAGE_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                        o.getApply(), dateFormat.format(o.getApplyTime()),
                        o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime())));
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
                    case STORAGE_IN_ORDER:
                        TStorageOrder o = storageOrderRepository.find(oa.getOid());
                        list2.add(createOrder(OrderType.STORAGE_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()),
                                o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime())));
                        break;
                    case PRODUCT_IN_ORDER:
                        break;
                    case AGREEMENT_IN_ORDER:
                        break;
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
                    case STORAGE_IN_ORDER:
                        TStorageOrder o = storageOrderRepository.find(or.getOid());
                        list2.add(createOrder(OrderType.STORAGE_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()),
                                o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime())));
                        break;
                    case PRODUCT_IN_ORDER:
                        break;
                    case AGREEMENT_IN_ORDER:
                        break;
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
                    case STORAGE_IN_ORDER:
                        TStorageOrder o = storageOrderRepository.find(oa.getOid());
                        list2.add(createOrder(OrderType.STORAGE_IN_ORDER, o.getId(), o.getBatch(), o.getSid(), o.getValue(),
                                o.getApply(), dateFormat.format(o.getApplyTime()),
                                o.getReview(), null == o.getReview() ? null : dateFormat.format(o.getReviewTime())));
                        break;
                    case PRODUCT_IN_ORDER:
                        break;
                    case AGREEMENT_IN_ORDER:
                        break;
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

        HashMap<String, Object> datas = storageOrderService.find(id);
        if (null != datas) {
            ret.put("comms", datas.get("comms"));
            ret.put("attrs", datas.get("attrs"));
        }
        return ret;
    }
}
