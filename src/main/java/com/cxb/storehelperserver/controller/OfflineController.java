package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.agreement.*;
import com.cxb.storehelperserver.controller.request.sale.*;
import com.cxb.storehelperserver.model.TAgreementOrder;
import com.cxb.storehelperserver.model.TSaleOrder;
import com.cxb.storehelperserver.service.SaleService;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.cxb.storehelperserver.util.TypeDefine.OrderType.*;

/**
 * desc: 线下销售接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/sale")
public class OfflineController {
    @Resource
    private SaleService saleService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/offline")
    public RestResult offline(@Validated @RequestBody OfflineValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TAgreementOrder order = new TAgreementOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setAid(req.getAid());
        order.setAsid(0);
        order.setOtype(AGREEMENT_OFFLINE_ORDER.getValue());
        order.setApply(req.getId());
        order.setPayPrice(new BigDecimal(0));
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.offline(req.getId(), order, req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setOffline")
    public RestResult setOffline(@Validated @RequestBody SetOfflineValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.setOffline(req.getId(), req.getOid(), req.getSid(), req.getSid2(), applyTime, req.getCommoditys(),
                req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delOffline")
    public RestResult delOffline(@Validated @RequestBody DelOfflineValid req) {
        return agreementService.delOffline(req.getId(), req.getOid());
    }

    @PostMapping("/reviewOffline")
    public RestResult reviewOffline(@Validated @RequestBody ReviewOfflineValid req) {
        return agreementService.reviewOffline(req.getId(), req.getOid());
    }

    @PostMapping("/revokeOffline")
    public RestResult revokeOffline(@Validated @RequestBody RevokeOfflineValid req) {
        return agreementService.revokeOffline(req.getId(), req.getOid());
    }

    @PostMapping("/setOfflinePay")
    public RestResult setOfflinePay(@Validated @RequestBody SetOfflinePayValid req) {
        return agreementService.setOfflinePay(req.getId(), req.getOid(), req.getPay());
    }

    @PostMapping("/backc")
    public RestResult backc(@Validated @RequestBody BackValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TAgreementOrder order = new TAgreementOrder();
        order.setRid(req.getRid());
        order.setOtype(AGREEMENT_BACK_ORDER.getValue());
        order.setApply(req.getId());
        order.setPayPrice(new BigDecimal(0));
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.backc(req.getId(), order, req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setBack")
    public RestResult setBack(@Validated @RequestBody SetBackValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return agreementService.setBack(req.getId(), req.getOid(), applyTime, req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delBack")
    public RestResult delBack(@Validated @RequestBody DelBackValid req) {
        return agreementService.delBack(req.getId(), req.getOid());
    }

    @PostMapping("/reviewBack")
    public RestResult reviewBack(@Validated @RequestBody ReviewBackValid req) {
        return agreementService.reviewBack(req.getId(), req.getOid());
    }

    @PostMapping("/revokeBack")
    public RestResult revokeBack(@Validated @RequestBody RevokeBackValid req) {
        return agreementService.revokeBack(req.getId(), req.getOid());
    }
}
