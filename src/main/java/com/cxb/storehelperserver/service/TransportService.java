package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.TypeDefine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.Date;

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
    private AgreementOrderService agreementOrderService;

    @Resource
    private OfflineOrderService offlineOrderService;

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
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseFareRepository purchaseFareRepository;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageFareRepository storageFareRepository;

    public RestResult addOrderFare(int id, TypeDefine.BusinessType otype, int oid, String ship, String code, String phone, BigDecimal fare, String remark) {
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

    public RestResult delOrderFare(int id, TypeDefine.BusinessType otype, int oid, int fid) {
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
                if (null != fare.getReview()) {
                    if (!fare.getReview().equals(id)) {
                        return RestResult.fail("要删除已审核信息，请联系审核人");
                    }
                } else {
                    if (!order.getApply().equals(id)) {
                        return RestResult.fail("只能由申请人删除信息");
                    }
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
}
