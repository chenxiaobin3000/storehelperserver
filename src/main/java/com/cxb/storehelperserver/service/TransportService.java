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

import com.cxb.storehelperserver.util.TypeDefine.BusinessType;
import com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc:
 * auth: cxb
 * date: 2023/4/24
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TransportService {
    @Resource
    private CheckService checkService;

    @Resource
    private OrderService orderService;

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
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementFareRepository agreementFareRepository;

    @Resource
    private OfflineOrderRepository offlineOrderRepository;

    @Resource
    private OfflineFareRepository offlineFareRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductFareRepository productFareRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseFareRepository purchaseFareRepository;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageFareRepository storageFareRepository;

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

                // 运费
                if (!offlineFareRepository.insert(oid, ship, code, phone, fare, remark, new Date())) {
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

                // 运费
                if (!productFareRepository.insert(oid, ship, code, phone, fare, remark, new Date())) {
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

    public RestResult setOrderFare(int id, int fid, BusinessType otype, int oid, String ship, String code, String phone, BigDecimal farePrice, String remark) {
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

                // 运费由申请人修改
                TAgreementFare fare = agreementFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人修改信息");
                }
                fare.setShip(ship);
                fare.setCode(code);
                fare.setPhone(phone);
                fare.setFare(farePrice);
                fare.setRemark(remark);
                if (!agreementFareRepository.update(fare)) {
                    return RestResult.fail("添加物流费用失败");
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

                // 运费由申请人修改
                TOfflineFare fare = offlineFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人修改信息");
                }
                fare.setShip(ship);
                fare.setCode(code);
                fare.setPhone(phone);
                fare.setFare(farePrice);
                fare.setRemark(remark);
                if (!offlineFareRepository.update(fare)) {
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

                // 运费由申请人修改
                TPurchaseFare fare = purchaseFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人修改信息");
                }
                fare.setShip(ship);
                fare.setCode(code);
                fare.setPhone(phone);
                fare.setFare(farePrice);
                fare.setRemark(remark);
                if (!purchaseFareRepository.update(fare)) {
                    return RestResult.fail("添加物流费用失败");
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

                // 运费由申请人修改
                TProductFare fare = productFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人修改信息");
                }
                fare.setShip(ship);
                fare.setCode(code);
                fare.setPhone(phone);
                fare.setFare(farePrice);
                fare.setRemark(remark);
                if (!productFareRepository.update(fare)) {
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

                // 运费由申请人修改
                TStorageFare fare = storageFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人修改信息");
                }
                fare.setShip(ship);
                fare.setCode(code);
                fare.setPhone(phone);
                fare.setFare(farePrice);
                fare.setRemark(remark);
                if (!storageFareRepository.update(fare)) {
                    return RestResult.fail("添加物流费用失败");
                }
                return RestResult.ok();
            }
            default:
                break;
        }
        return RestResult.fail("不支持修改运费");
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
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人删除信息");
                }
                if (!agreementFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
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

                // 运费由申请人删，已审核由审核人删，备注由审核人删
                TOfflineFare fare = offlineFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人删除信息");
                }
                if (!offlineFareRepository.delete(fid)) {
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
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人删除信息");
                }
                if (!purchaseFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
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

                // 运费由申请人删，已审核由审核人删，备注由审核人删
                TProductFare fare = productFareRepository.find(fid);
                if (null == fare) {
                    return RestResult.fail("未查询到运费信息");
                }
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人删除信息");
                }
                if (!productFareRepository.delete(fid)) {
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
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人删除信息");
                }
                if (!storageFareRepository.delete(fid)) {
                    return RestResult.fail("删除运费信息失败");
                }
                return RestResult.ok();
            }
            default:
                break;
        }
        return RestResult.fail("不支持删除运费");
    }

    public RestResult getAgreementFareList(int id, int gid, int aid, int sid, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end) {
        RestResult ret = check(id, gid);
        if (null != ret) {
            return ret;
        }

        int total = agreementFareRepository.total(gid, aid, sid, type, review, complete, start, end);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        val list = agreementFareRepository.pagination(gid, aid, sid, type, page, limit, review, complete, start, end);
        val list2 = new ArrayList<HashMap<String, Object>>();
        for (TAgreementFare fare : list) {
            val tmp = createFare(fare.getId(), fare.getOid(), fare.getShip(), fare.getCode(), fare.getPhone(), fare.getFare(), fare.getRemark(), fare.getCdate(), simpleDateFormat);
            val order = orderService.createOrder(type, fare.getOid());
            tmp.put("batch", order.get("batch"));
            tmp.put("comms", order.get("comms"));
            tmp.put("attrs", order.get("attrs"));
            tmp.put("total", order.get("total"));
            tmp.put("remarks", order.get("remarks"));
            tmp.put("sname", order.get("sname"));
            list2.add(tmp);
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getOfflineFareList(int id, int gid, int aid, int sid, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end) {
        RestResult ret = check(id, gid);
        if (null != ret) {
            return ret;
        }

        int total = offlineFareRepository.total(gid, aid, sid, type, review, complete, start, end);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        val list = offlineFareRepository.pagination(gid, aid, sid, type, page, limit, review, complete, start, end);
        val list2 = new ArrayList<HashMap<String, Object>>();
        for (TOfflineFare fare : list) {
            val tmp = createFare(fare.getId(), fare.getOid(), fare.getShip(), fare.getCode(), fare.getPhone(), fare.getFare(), fare.getRemark(), fare.getCdate(), simpleDateFormat);
            val order = orderService.createOrder(type, fare.getOid());
            tmp.put("batch", order.get("batch"));
            tmp.put("comms", order.get("comms"));
            tmp.put("attrs", order.get("attrs"));
            tmp.put("total", order.get("total"));
            tmp.put("remarks", order.get("remarks"));
            tmp.put("sname", order.get("sname"));
            list2.add(tmp);
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getProductFareList(int id, int gid, int sid, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end) {
        RestResult ret = check(id, gid);
        if (null != ret) {
            return ret;
        }

        int total = productFareRepository.total(gid, sid, type, review, complete, start, end);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        val list = productFareRepository.pagination(gid, sid, type, page, limit, review, complete, start, end);
        val list2 = new ArrayList<HashMap<String, Object>>();
        for (TProductFare fare : list) {
            val tmp = createFare(fare.getId(), fare.getOid(), fare.getShip(), fare.getCode(), fare.getPhone(), fare.getFare(), fare.getRemark(), fare.getCdate(), simpleDateFormat);
            val order = orderService.createOrder(type, fare.getOid());
            tmp.put("batch", order.get("batch"));
            tmp.put("comms", order.get("comms"));
            tmp.put("attrs", order.get("attrs"));
            tmp.put("total", order.get("total"));
            tmp.put("remarks", order.get("remarks"));
            tmp.put("sname", order.get("sname"));
            list2.add(tmp);
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getPurchaseFareList(int id, int gid, int sid, int supplier, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end) {
        RestResult ret = check(id, gid);
        if (null != ret) {
            return ret;
        }

        int total = purchaseFareRepository.total(gid, sid, supplier, type, review, complete, start, end);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        val list = purchaseFareRepository.pagination(gid, sid, supplier, type, page, limit, review, complete, start, end);
        val list2 = new ArrayList<HashMap<String, Object>>();
        for (TPurchaseFare fare : list) {
            val tmp = createFare(fare.getId(), fare.getOid(), fare.getShip(), fare.getCode(), fare.getPhone(), fare.getFare(), fare.getRemark(), fare.getCdate(), simpleDateFormat);
            val order = orderService.createOrder(type, fare.getOid());
            tmp.put("batch", order.get("batch"));
            tmp.put("comms", order.get("comms"));
            tmp.put("attrs", order.get("attrs"));
            tmp.put("total", order.get("total"));
            tmp.put("remarks", order.get("remarks"));
            tmp.put("sname", order.get("sname"));
            list2.add(tmp);
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getStorageFareList(int id, int gid, int sid, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end) {
        RestResult ret = check(id, gid);
        if (null != ret) {
            return ret;
        }

        int total = storageFareRepository.total(gid, sid, type, review, complete, start, end);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        val list = storageFareRepository.pagination(gid, sid, type, page, limit, review, complete, start, end);
        val list2 = new ArrayList<HashMap<String, Object>>();
        for (TStorageFare fare : list) {
            val tmp = createFare(fare.getId(), fare.getOid(), fare.getShip(), fare.getCode(), fare.getPhone(), fare.getFare(), fare.getRemark(), fare.getCdate(), simpleDateFormat);
            val order = orderService.createOrder(type, fare.getOid());
            tmp.put("batch", order.get("batch"));
            tmp.put("comms", order.get("comms"));
            tmp.put("attrs", order.get("attrs"));
            tmp.put("total", order.get("total"));
            tmp.put("remarks", order.get("remarks"));
            tmp.put("sname", order.get("sname"));
            list2.add(tmp);
        }
        return RestResult.ok(new PageData(total, list2));
    }

    private RestResult check(int id, int gid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return null;
    }

    private HashMap<String, Object> createFare(int id, int oid, String ship, String code, String phone, BigDecimal fare, String remark, Date cdate, SimpleDateFormat simpleDateFormat) {
        val tmp = new HashMap<String, Object>();
        tmp.put("id", id);
        tmp.put("oid", oid);
        tmp.put("ship", ship);
        tmp.put("code", code);
        tmp.put("phone", phone);
        tmp.put("fare", fare);
        tmp.put("remark", remark);
        tmp.put("cdate", simpleDateFormat.format(cdate));
        return tmp;
    }
}
