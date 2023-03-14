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

import static com.cxb.storehelperserver.util.TypeDefine.OrderType.*;

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
        return purchaseService.purchase(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getPrices(),
                req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
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
        return purchaseService.setPurchase(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(),
                req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
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

    @PostMapping("/purchase2")
    public RestResult purchase2(@Validated @RequestBody PurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TPurchaseOrder order = new TPurchaseOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(PURCHASE_PURCHASE2_ORDER.getValue());
        order.setApply(req.getId());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return purchaseService.purchase2(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getPrices(),
                req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setPurchase2")
    public RestResult setPurchase2(@Validated @RequestBody SetPurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return purchaseService.setPurchase2(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(),
                req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delPurchase2")
    public RestResult delPurchase2(@Validated @RequestBody DelPurchaseValid req) {
        return purchaseService.delPurchase2(req.getId(), req.getOid());
    }

    @PostMapping("/reviewPurchase2")
    public RestResult reviewPurchase2(@Validated @RequestBody ReviewPurchaseValid req) {
        return purchaseService.reviewPurchase2(req.getId(), req.getOid());
    }

    @PostMapping("/revokePurchase2")
    public RestResult revokePurchase2(@Validated @RequestBody RevokePurchaseValid req) {
        return purchaseService.revokePurchase2(req.getId(), req.getOid());
    }

    @PostMapping("/addPurchase2Info")
    public RestResult addPurchase2Info(@Validated @RequestBody AddPurchaseInfoValid req) {
        return purchaseService.addPurchase2Info(req.getId(), req.getOid(), req.getFare(), req.getRemark());
    }

    @PostMapping("/delPurchase2Info")
    public RestResult delPurchase2Info(@Validated @RequestBody DelPurchaseInfoValid req) {
        return purchaseService.delPurchase2Info(req.getId(), req.getOid(), req.getFid(), req.getRid());
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
        return purchaseService.returnc(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
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
        return purchaseService.setReturn(req.getId(), req.getOid(), applyTime, req.getTypes(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
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

    @PostMapping("/returnc2")
    public RestResult returnc2(@Validated @RequestBody ReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TPurchaseOrder order = new TPurchaseOrder();
        order.setOtype(PURCHASE_RETURN2_ORDER.getValue());
        order.setApply(req.getId());
        order.setRid(req.getRid());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return purchaseService.returnc2(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setReturn2")
    public RestResult setReturn2(@Validated @RequestBody SetReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return purchaseService.setReturn2(req.getId(), req.getOid(), applyTime, req.getTypes(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delReturn2")
    public RestResult delReturn2(@Validated @RequestBody DelReturnValid req) {
        return purchaseService.delReturn2(req.getId(), req.getOid());
    }

    @PostMapping("/reviewReturn2")
    public RestResult reviewReturn2(@Validated @RequestBody ReviewReturnValid req) {
        return purchaseService.reviewReturn2(req.getId(), req.getOid());
    }

    @PostMapping("/revokeReturn2")
    public RestResult revokeReturn2(@Validated @RequestBody RevokeReturnValid req) {
        return purchaseService.revokeReturn2(req.getId(), req.getOid());
    }

    @PostMapping("/addReturn2Info")
    public RestResult addReturn2Info(@Validated @RequestBody AddPurchaseInfoValid req) {
        return purchaseService.addReturn2Info(req.getId(), req.getOid(), req.getFare(), req.getRemark());
    }

    @PostMapping("/delReturn2Info")
    public RestResult delReturn2Info(@Validated @RequestBody DelPurchaseInfoValid req) {
        return purchaseService.delReturn2Info(req.getId(), req.getOid(), req.getFid(), req.getRid());
    }
}
