package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.purchase.*;
import com.cxb.storehelperserver.model.TPurchaseOrder;
import com.cxb.storehelperserver.service.PurchaseService;
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

import static com.cxb.storehelperserver.util.TypeDefine.OrderType.PURCHASE_PURCHASE_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.PURCHASE_RETURN_ORDER;

/**
 * desc: 采购接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {
    @Resource
    private PurchaseService purchaseService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/purchase")
    public RestResult purchase(@Validated @RequestBody PurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TPurchaseOrder order = new TPurchaseOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(PURCHASE_PURCHASE_ORDER.getValue());
        order.setApply(req.getId());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return purchaseService.purchase(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
    }

    @PostMapping("/setPurchase")
    public RestResult setPurchase(@Validated @RequestBody SetPurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return purchaseService.setPurchase(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
    }

    @PostMapping("/delPurchase")
    public RestResult delPurchase(@Validated @RequestBody DelPurchaseValid req) {
        return purchaseService.delPurchase(req.getId(), req.getOid());
    }

    @PostMapping("/reviewPurchase")
    public RestResult reviewPurchase(@Validated @RequestBody ReviewPurchaseValid req) {
        return purchaseService.reviewPurchase(req.getId(), req.getOid());
    }

    @PostMapping("/revokePurchase")
    public RestResult revokePurchase(@Validated @RequestBody RevokePurchaseValid req) {
        return purchaseService.revokePurchase(req.getId(), req.getOid());
    }

    @PostMapping("/addPurchaseInfo")
    public RestResult addPurchaseInfo(@Validated @RequestBody AddPurchaseInfoValid req) {
        return purchaseService.addPurchaseInfo(req.getId(), req.getOid(), req.getFare(), req.getRemark());
    }

    @PostMapping("/delPurchaseInfo")
    public RestResult delPurchaseInfo(@Validated @RequestBody DelPurchaseInfoValid req) {
        return purchaseService.delPurchaseInfo(req.getId(), req.getOid(), req.getFid(), req.getRid());
    }

    @PostMapping("/returnc")
    public RestResult returnc(@Validated @RequestBody ReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TPurchaseOrder order = new TPurchaseOrder();
        order.setOtype(PURCHASE_RETURN_ORDER.getValue());
        order.setApply(req.getId());
        order.setRid(req.getRid());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return purchaseService.returnc(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
    }

    @PostMapping("/setReturn")
    public RestResult setReturn(@Validated @RequestBody SetReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return purchaseService.setReturn(req.getId(), req.getOid(), applyTime, req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
    }

    @PostMapping("/delReturn")
    public RestResult delReturn(@Validated @RequestBody DelReturnValid req) {
        return purchaseService.delReturn(req.getId(), req.getOid());
    }

    @PostMapping("/reviewReturn")
    public RestResult reviewReturn(@Validated @RequestBody ReviewReturnValid req) {
        return purchaseService.reviewReturn(req.getId(), req.getOid());
    }

    @PostMapping("/revokeReturn")
    public RestResult revokeReturn(@Validated @RequestBody RevokeReturnValid req) {
        return purchaseService.revokeReturn(req.getId(), req.getOid());
    }

    @PostMapping("/addReturnInfo")
    public RestResult addReturnInfo(@Validated @RequestBody AddPurchaseInfoValid req) {
        return purchaseService.addReturnInfo(req.getId(), req.getOid(), req.getFare(), req.getRemark());
    }

    @PostMapping("/delReturnInfo")
    public RestResult delReturnInfo(@Validated @RequestBody DelPurchaseInfoValid req) {
        return purchaseService.delReturnInfo(req.getId(), req.getOid(), req.getFid(), req.getRid());
    }
}
