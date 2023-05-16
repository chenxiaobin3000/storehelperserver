package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.agreement.*;
import com.cxb.storehelperserver.model.TAgreementOrder;
import com.cxb.storehelperserver.service.AgreementService;
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
 * desc: 履约接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/agreement")
public class AgreementController {
    @Resource
    private AgreementService agreementService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/shipped")
    public RestResult shipped(@Validated @RequestBody ShippedValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TAgreementOrder order = new TAgreementOrder();
        order.setGid(req.getGid());
        order.setAid(req.getAid());
        order.setOtype(AGREEMENT_SHIPPED_ORDER.getValue());
        order.setApply(req.getId());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.shipped(req.getId(), order, req.getSid(), req.getReview(), req.getStorage(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setShipped")
    public RestResult setShipped(@Validated @RequestBody SetShippedValid req) {
        return agreementService.setShipped(req.getId(), req.getOid(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues());
    }

    @PostMapping("/delShipped")
    public RestResult delShipped(@Validated @RequestBody DelShippedValid req) {
        return agreementService.delShipped(req.getId(), req.getOid());
    }

    @PostMapping("/reviewShipped")
    public RestResult reviewShipped(@Validated @RequestBody ReviewShippedValid req) {
        return agreementService.reviewShipped(req.getId(), req.getOid());
    }

    @PostMapping("/revokeShipped")
    public RestResult revokeShipped(@Validated @RequestBody RevokeShippedValid req) {
        return agreementService.revokeShipped(req.getId(), req.getOid());
    }

    @PostMapping("/returnc")
    public RestResult returnc(@Validated @RequestBody ReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TAgreementOrder order = new TAgreementOrder();
        order.setOtype(AGREEMENT_RETURN_ORDER.getValue());
        order.setApply(req.getId());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.returnc(req.getId(), order, req.getRid(), req.getSid(), req.getReview(), req.getStorage(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setReturn")
    public RestResult setReturn(@Validated @RequestBody SetReturnValid req) {
        return agreementService.setReturn(req.getId(), req.getOid(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues());
    }

    @PostMapping("/delReturn")
    public RestResult delReturn(@Validated @RequestBody DelReturnValid req) {
        return agreementService.delReturn(req.getId(), req.getOid());
    }

    @PostMapping("/reviewReturn")
    public RestResult reviewReturn(@Validated @RequestBody ReviewReturnValid req) {
        return agreementService.reviewReturn(req.getId(), req.getOid());
    }

    @PostMapping("/revokeReturn")
    public RestResult revokeReturn(@Validated @RequestBody RevokeReturnValid req) {
        return agreementService.revokeReturn(req.getId(), req.getOid());
    }
}
