package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.offline.*;
import com.cxb.storehelperserver.model.TOfflineOrder;
import com.cxb.storehelperserver.service.OfflineService;
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
@RequestMapping("/api/offline")
public class OfflineController {
    @Resource
    private OfflineService offlineService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/offline")
    public RestResult offline(@Validated @RequestBody OfflineValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TOfflineOrder order = new TOfflineOrder();
        order.setGid(req.getGid());
        order.setAid(req.getAid());
        order.setOtype(OFFLINE_OFFLINE_ORDER.getValue());
        order.setApply(req.getId());
        order.setPayPrice(new BigDecimal(0));
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return offlineService.offline(req.getId(), order, req.getSid(), req.getReview(), req.getStorage(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
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
        return offlineService.setOffline(req.getId(), req.getOid(), req.getAid(), applyTime, req.getCommoditys(),
                req.getPrices(), req.getWeights(), req.getNorms(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delOffline")
    public RestResult delOffline(@Validated @RequestBody DelOfflineValid req) {
        return offlineService.delOffline(req.getId(), req.getOid());
    }

    @PostMapping("/reviewOffline")
    public RestResult reviewOffline(@Validated @RequestBody ReviewOfflineValid req) {
        return offlineService.reviewOffline(req.getId(), req.getOid());
    }

    @PostMapping("/revokeOffline")
    public RestResult revokeOffline(@Validated @RequestBody RevokeOfflineValid req) {
        return offlineService.revokeOffline(req.getId(), req.getOid());
    }

    @PostMapping("/setOfflinePay")
    public RestResult setOfflinePay(@Validated @RequestBody SetOfflinePayValid req) {
        return offlineService.setOfflinePay(req.getId(), req.getOid(), req.getPay());
    }

    @PostMapping("/returnc")
    public RestResult returnc(@Validated @RequestBody ReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TOfflineOrder order = new TOfflineOrder();
        order.setOtype(OFFLINE_RETURN_ORDER.getValue());
        order.setApply(req.getId());
        order.setPayPrice(new BigDecimal(0));
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return offlineService.returnc(req.getId(), order, req.getRid(), req.getSid(), req.getReview(), req.getStorage(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
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
        return offlineService.setReturn(req.getId(), req.getOid(), applyTime, req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delReturn")
    public RestResult delReturn(@Validated @RequestBody DelReturnValid req) {
        return offlineService.delReturn(req.getId(), req.getOid());
    }

    @PostMapping("/reviewReturn")
    public RestResult reviewReturn(@Validated @RequestBody ReviewReturnValid req) {
        return offlineService.reviewReturn(req.getId(), req.getOid());
    }

    @PostMapping("/revokeReturn")
    public RestResult revokeReturn(@Validated @RequestBody RevokeReturnValid req) {
        return offlineService.revokeReturn(req.getId(), req.getOid());
    }
}
