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

import static com.cxb.storehelperserver.util.TypeDefine.OrderType.AGREEMENT_RETURN_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.AGREEMENT_SHIPPED_ORDER;

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
        order.setSid(req.getSid());
        order.setOtype(AGREEMENT_SHIPPED_ORDER.getValue());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.shipped(req.getId(), order, req.getFare(), req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
    }

    @PostMapping("/setShipped")
    public RestResult setShipped(@Validated @RequestBody SetShippedValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TAgreementOrder order = new TAgreementOrder();
        order.setId(req.getOid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.setShipped(req.getId(), order, req.getFare(), req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
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
        order.setRid(req.getRid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.returnc(req.getId(), order, req.getFare(), req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
    }

    @PostMapping("/setReturn")
    public RestResult setReturn(@Validated @RequestBody SetReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TAgreementOrder order = new TAgreementOrder();
        order.setId(req.getOid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.setReturn(req.getId(), order, req.getFare(), req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
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
