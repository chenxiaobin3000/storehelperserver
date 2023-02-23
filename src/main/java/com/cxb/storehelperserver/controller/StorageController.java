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
        storage.setContact(req.getContact());
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
        return storageMgrService.addStorage(req.getId(), storage);
    }

    @PostMapping("/setStorage")
    public RestResult setStorage(@Validated @RequestBody SetStorageValid req) {
        TStorage storage = new TStorage();
        storage.setId(req.getSid());
        storage.setGid(req.getGid());
        storage.setArea(Long.valueOf(req.getArea()));
        storage.setContact(req.getContact());
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
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

    @PostMapping("/purchase")
    public RestResult purchase(@Validated @RequestBody PurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(STORAGE_PURCHASE_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(req.getPid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.purchase(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setPurchase")
    public RestResult setPurchase(@Validated @RequestBody SetPurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setId(req.getOid());
        order.setSid(req.getSid());
        order.setOid(req.getPid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.setPurchase(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delPurchase")
    public RestResult delPurchase(@Validated @RequestBody DelPurchaseValid req) {
        return storageService.delPurchase(req.getId(), req.getOid());
    }

    @PostMapping("/reviewPurchase")
    public RestResult reviewPurchase(@Validated @RequestBody ReviewPurchaseValid req) {
        return storageService.reviewPurchase(req.getId(), req.getOid());
    }

    @PostMapping("/revokePurchase")
    public RestResult revokePurchase(@Validated @RequestBody RevokePurchaseValid req) {
        return storageService.revokePurchase(req.getId(), req.getOid());
    }

    @PostMapping("/dispatch")
    public RestResult dispatch(@Validated @RequestBody DispatchValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(STORAGE_DISPATCH_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(0);
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.dispatch(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setDispatch")
    public RestResult setDispatch(@Validated @RequestBody SetDispatchValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setId(req.getOid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.setDispatch(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delDispatch")
    public RestResult delDispatch(@Validated @RequestBody DelDispatchValid req) {
        return storageService.delDispatch(req.getId(), req.getOid());
    }

    @PostMapping("/reviewDispatch")
    public RestResult reviewDispatch(@Validated @RequestBody ReviewDispatchValid req) {
        return storageService.reviewDispatch(req.getId(), req.getOid());
    }

    @PostMapping("/revokeDispatch")
    public RestResult revokeDispatch(@Validated @RequestBody RevokeDispatchValid req) {
        return storageService.revokeDispatch(req.getId(), req.getOid());
    }

    @PostMapping("/loss")
    public RestResult loss(@Validated @RequestBody LossValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(STORAGE_LOSS_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(0);
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.loss(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setLoss")
    public RestResult setLoss(@Validated @RequestBody SetLossValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setId(req.getOid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.setLoss(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
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

    @PostMapping("/returnc")
    public RestResult returnc(@Validated @RequestBody ReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(STORAGE_RETURN_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(req.getRid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.returnc(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setReturn")
    public RestResult setReturn(@Validated @RequestBody SetReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setId(req.getOid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return storageService.setReturn(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delReturn")
    public RestResult delReturn(@Validated @RequestBody DelReturnValid req) {
        return storageService.delReturn(req.getId(), req.getOid());
    }

    @PostMapping("/reviewReturn")
    public RestResult reviewReturn(@Validated @RequestBody ReviewReturnValid req) {
        return storageService.reviewReturn(req.getId(), req.getOid());
    }

    @PostMapping("/revokeReturn")
    public RestResult revokeReturn(@Validated @RequestBody RevokeReturnValid req) {
        return storageService.revokeReturn(req.getId(), req.getOid());
    }
}
