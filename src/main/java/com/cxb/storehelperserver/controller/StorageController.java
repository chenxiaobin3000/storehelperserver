package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.storage.*;
import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.model.TStorageOrder;
import com.cxb.storehelperserver.service.StorageMgrService;
import com.cxb.storehelperserver.service.StorageService;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.cxb.storehelperserver.util.TypeDefine.OrderType;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.*;

/**
 * desc: 仓库接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/storage")
public class StorageController {
    @Resource
    private StorageMgrService storageMgrService;

    @Resource
    private StorageService storageService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/addStorage")
    public RestResult addStorage(@Validated @RequestBody AddStorageValid req) {
        TStorage storage = new TStorage();
        storage.setGid(req.getGid());
        storage.setArea(Long.valueOf(req.getArea()));
        storage.setName(req.getName());
        storage.setContact(req.getContact());
        storage.setPhone(req.getPhone());
        storage.setAddress(req.getAddress());
        storage.setRemark(req.getRemark());
        return storageMgrService.addStorage(req.getId(), storage);
    }

    @PostMapping("/setStorage")
    public RestResult setStorage(@Validated @RequestBody SetStorageValid req) {
        TStorage storage = new TStorage();
        storage.setId(req.getSid());
        storage.setGid(req.getGid());
        storage.setArea(Long.valueOf(req.getArea()));
        storage.setName(req.getName());
        storage.setContact(req.getContact());
        storage.setPhone(req.getPhone());
        storage.setAddress(req.getAddress());
        storage.setRemark(req.getRemark());
        return storageMgrService.setStorage(req.getId(), storage);
    }

    @PostMapping("/delStorage")
    public RestResult delStorage(@Validated @RequestBody DelStorageValid req) {
        return storageMgrService.delStorage(req.getId(), req.getGid(), req.getSid());
    }

    @PostMapping("/getGroupStorage")
    public RestResult getGroupStorage(@Validated @RequestBody GetGroupStorageValid req) {
        return storageMgrService.getGroupStorage(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getGroupAllStorage")
    public RestResult getGroupAllStorage(@Validated @RequestBody GetGroupAllStorageValid req) {
        return storageMgrService.getGroupAllStorage(req.getId());
    }

    @PostMapping("/getStorageType")
    public RestResult getStorageType(@Validated @RequestBody GetStorageTypeValid req) {
        return storageService.getStorageType(req.getId(), req.getGid());
    }

    @PostMapping("/sin")
    public RestResult sin(@Validated @RequestBody InValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setSid(req.getSid());
        order.setTid(0);
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_IN_ORDER:
                order.setOtype(STORAGE_PURCHASE_IN_ORDER.getValue());
                return storageService.purchaseIn(req.getId(), order, req.getOid(), req.getReview(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
            case STORAGE_PRODUCT_IN_ORDER:
                order.setOtype(STORAGE_PURCHASE_IN_ORDER.getValue());
                return storageService.productIn(req.getId(), order, req.getOid(), req.getReview(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
            case STORAGE_AGREEMENT_IN_ORDER:
                order.setOtype(STORAGE_PURCHASE_IN_ORDER.getValue());
                return storageService.agreementIn(req.getId(), order, req.getOid(), req.getReview(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
            case STORAGE_OFFLINE_IN_ORDER:
                order.setOtype(STORAGE_PURCHASE_IN_ORDER.getValue());
                return storageService.offlineIn(req.getId(), order, req.getOid(), req.getReview(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/setIn")
    public RestResult setIn(@Validated @RequestBody SetInValid req) {
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_IN_ORDER:
                return storageService.setPurchaseIn(req.getId(), req.getType(), req.getOid(), req.getCommoditys(), req.getWeights(), req.getValues());
            case STORAGE_PRODUCT_IN_ORDER:
                return storageService.setProductIn(req.getId(), req.getType(), req.getOid(), req.getCommoditys(), req.getWeights(), req.getValues());
            case STORAGE_AGREEMENT_IN_ORDER:
                return storageService.setAgreementIn(req.getId(), req.getType(), req.getOid(), req.getCommoditys(), req.getWeights(), req.getValues());
            case STORAGE_OFFLINE_IN_ORDER:
                return storageService.setOfflineIn(req.getId(), req.getType(), req.getOid(), req.getCommoditys(), req.getWeights(), req.getValues());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/delIn")
    public RestResult delIn(@Validated @RequestBody DelInValid req) {
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_IN_ORDER:
                return storageService.delPurchaseIn(req.getId(), req.getOid());
            case STORAGE_PRODUCT_IN_ORDER:
                return storageService.delProductIn(req.getId(), req.getOid());
            case STORAGE_AGREEMENT_IN_ORDER:
                return storageService.delAgreementIn(req.getId(), req.getOid());
            case STORAGE_OFFLINE_IN_ORDER:
                return storageService.delOfflineIn(req.getId(), req.getOid());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/reviewIn")
    public RestResult reviewIn(@Validated @RequestBody ReviewInValid req) {
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_IN_ORDER:
                return storageService.reviewPurchaseIn(req.getId(), req.getOid());
            case STORAGE_PRODUCT_IN_ORDER:
                return storageService.reviewProductIn(req.getId(), req.getOid());
            case STORAGE_AGREEMENT_IN_ORDER:
                return storageService.reviewAgreementIn(req.getId(), req.getOid());
            case STORAGE_OFFLINE_IN_ORDER:
                return storageService.reviewOfflineIn(req.getId(), req.getOid());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/revokeIn")
    public RestResult revokeIn(@Validated @RequestBody RevokeInValid req) {
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_IN_ORDER:
                return storageService.revokePurchaseIn(req.getId(), req.getOid());
            case STORAGE_PRODUCT_IN_ORDER:
                return storageService.revokeProductIn(req.getId(), req.getOid());
            case STORAGE_AGREEMENT_IN_ORDER:
                return storageService.revokeAgreementIn(req.getId(), req.getOid());
            case STORAGE_OFFLINE_IN_ORDER:
                return storageService.revokeOfflineIn(req.getId(), req.getOid());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/sout")
    public RestResult sout(@Validated @RequestBody OutValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setSid(req.getSid());
        order.setTid(0);
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_OUT_ORDER:
                order.setOtype(STORAGE_PURCHASE_OUT_ORDER.getValue());
                return storageService.purchaseOut(req.getId(), order, req.getOid(), req.getReview(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
            case STORAGE_PRODUCT_OUT_ORDER:
                order.setOtype(STORAGE_PRODUCT_OUT_ORDER.getValue());
                return storageService.productOut(req.getId(), order, req.getOid(), req.getReview(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
            case STORAGE_AGREEMENT_OUT_ORDER:
                order.setOtype(STORAGE_AGREEMENT_OUT_ORDER.getValue());
                return storageService.agreementOut(req.getId(), order, req.getOid(), req.getReview(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
            case STORAGE_OFFLINE_OUT_ORDER:
                order.setOtype(STORAGE_OFFLINE_OUT_ORDER.getValue());
                return storageService.offlineOut(req.getId(), order, req.getOid(), req.getReview(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/setOut")
    public RestResult setOut(@Validated @RequestBody SetOutValid req) {
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_OUT_ORDER:
                return storageService.setPurchaseOut(req.getId(), req.getType(), req.getOid(), req.getCommoditys(), req.getWeights(), req.getValues());
            case STORAGE_PRODUCT_OUT_ORDER:
                return storageService.setProductOut(req.getId(), req.getType(), req.getOid(), req.getCommoditys(), req.getWeights(), req.getValues());
            case STORAGE_AGREEMENT_OUT_ORDER:
                return storageService.setAgreementOut(req.getId(), req.getType(), req.getOid(), req.getCommoditys(), req.getWeights(), req.getValues());
            case STORAGE_OFFLINE_OUT_ORDER:
                return storageService.setOfflineOut(req.getId(), req.getType(), req.getOid(), req.getCommoditys(), req.getWeights(), req.getValues());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/delOut")
    public RestResult delOut(@Validated @RequestBody DelOutValid req) {
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_OUT_ORDER:
                return storageService.delPurchaseOut(req.getId(), req.getOid());
            case STORAGE_PRODUCT_OUT_ORDER:
                return storageService.delProductOut(req.getId(), req.getOid());
            case STORAGE_AGREEMENT_OUT_ORDER:
                return storageService.delAgreementOut(req.getId(), req.getOid());
            case STORAGE_OFFLINE_OUT_ORDER:
                return storageService.delOfflineOut(req.getId(), req.getOid());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/reviewOut")
    public RestResult reviewOut(@Validated @RequestBody ReviewOutValid req) {
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_OUT_ORDER:
                return storageService.reviewPurchaseOut(req.getId(), req.getOid());
            case STORAGE_PRODUCT_OUT_ORDER:
                return storageService.reviewProductOut(req.getId(), req.getOid());
            case STORAGE_AGREEMENT_OUT_ORDER:
                return storageService.reviewAgreementOut(req.getId(), req.getOid());
            case STORAGE_OFFLINE_OUT_ORDER:
                return storageService.reviewOfflineOut(req.getId(), req.getOid());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/revokeOut")
    public RestResult revokeOut(@Validated @RequestBody RevokeOutValid req) {
        switch (OrderType.valueOf(req.getType())) {
            case STORAGE_PURCHASE_OUT_ORDER:
                return storageService.revokePurchaseOut(req.getId(), req.getOid());
            case STORAGE_PRODUCT_OUT_ORDER:
                return storageService.revokeProductOut(req.getId(), req.getOid());
            case STORAGE_AGREEMENT_OUT_ORDER:
                return storageService.revokeAgreementOut(req.getId(), req.getOid());
            case STORAGE_OFFLINE_OUT_ORDER:
                return storageService.revokeOfflineOut(req.getId(), req.getOid());
            default:
                return RestResult.fail("未知订单类型");
        }
    }

    @PostMapping("/dispatchIn")
    public RestResult dispatchIn(@Validated @RequestBody DispatchInValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(STORAGE_DISPATCH_IN_ORDER.getValue());
        order.setTid(0);
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.dispatchIn(req.getId(), order, req.getReview(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setDispatchIn")
    public RestResult setDispatchIn(@Validated @RequestBody SetDispatchInValid req) {
        return storageService.setDispatchIn(req.getId(), req.getOid(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues());
    }

    @PostMapping("/delDispatchIn")
    public RestResult delDispatchIn(@Validated @RequestBody DelDispatchInValid req) {
        return storageService.delDispatchIn(req.getId(), req.getOid());
    }

    @PostMapping("/reviewDispatchIn")
    public RestResult reviewDispatchIn(@Validated @RequestBody ReviewDispatchInValid req) {
        return storageService.reviewDispatchIn(req.getId(), req.getOid());
    }

    @PostMapping("/revokeDispatchIn")
    public RestResult revokeDispatchIn(@Validated @RequestBody RevokeDispatchInValid req) {
        return storageService.revokeDispatchIn(req.getId(), req.getOid());
    }

    @PostMapping("/dispatchOut")
    public RestResult dispatchOut(@Validated @RequestBody DispatchOutValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(STORAGE_DISPATCH_OUT_ORDER.getValue());
        order.setTid(0);
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.dispatchOut(req.getId(), order, req.getReview(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setDispatchOut")
    public RestResult setDispatchOut(@Validated @RequestBody SetDispatchOutValid req) {
        return storageService.setDispatchOut(req.getId(), req.getOid(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues());
    }

    @PostMapping("/delDispatchOut")
    public RestResult delDispatchOut(@Validated @RequestBody DelDispatchOutValid req) {
        return storageService.delDispatchOut(req.getId(), req.getOid());
    }

    @PostMapping("/reviewDispatchOut")
    public RestResult reviewDispatchOut(@Validated @RequestBody ReviewDispatchOutValid req) {
        return storageService.reviewDispatchOut(req.getId(), req.getOid());
    }

    @PostMapping("/revokeDispatchOut")
    public RestResult revokeDispatchOut(@Validated @RequestBody RevokeDispatchOutValid req) {
        return storageService.revokeDispatchOut(req.getId(), req.getOid());
    }

    @PostMapping("/loss")
    public RestResult loss(@Validated @RequestBody LossValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(STORAGE_LOSS_ORDER.getValue());
        order.setTid(req.getTid());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.loss(req.getId(), order, req.getReview(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setLoss")
    public RestResult setLoss(@Validated @RequestBody SetLossValid req) {
        return storageService.setLoss(req.getId(), req.getOid(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues());
    }

    @PostMapping("/delLoss")
    public RestResult delLoss(@Validated @RequestBody DelLossValid req) {
        return storageService.delLoss(req.getId(), req.getOid());
    }

    @PostMapping("/reviewLoss")
    public RestResult reviewLoss(@Validated @RequestBody ReviewLossValid req) {
        return storageService.reviewLoss(req.getId(), req.getOid());
    }

    @PostMapping("/revokeLoss")
    public RestResult revokeLoss(@Validated @RequestBody RevokeLossValid req) {
        return storageService.revokeLoss(req.getId(), req.getOid());
    }
}
