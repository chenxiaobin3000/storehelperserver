package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.transport.*;
import com.cxb.storehelperserver.service.TransportService;
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

import static com.cxb.storehelperserver.util.TypeDefine.BusinessType;

import com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc:
 * auth: cxb
 * date: 2023/4/24
 */
@Slf4j
@RestController
@RequestMapping("/api/transport")
public class TransportController {
    @Resource
    private TransportService transportService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/addOrderFare")
    public RestResult addOrderFare(@Validated @RequestBody AddOrderFareValid req) {
        return transportService.addOrderFare(req.getId(), BusinessType.valueOf(req.getOtype()), req.getOid(), req.getShip(),
                req.getCode(), req.getPhone(), req.getFare(), req.getRemark());
    }

    @PostMapping("/setOrderFare")
    public RestResult setOrderFare(@Validated @RequestBody SetOrderFareValid req) {
        return transportService.setOrderFare(req.getId(), req.getFid(), BusinessType.valueOf(req.getOtype()), req.getOid(), req.getShip(),
                req.getCode(), req.getPhone(), req.getFare(), req.getRemark());
    }

    @PostMapping("/delOrderFare")
    public RestResult delOrderFare(@Validated @RequestBody DelOrderFareValid req) {
        return transportService.delOrderFare(req.getId(), BusinessType.valueOf(req.getOtype()), req.getOid(), req.getFid());
    }

    @PostMapping("/getAgreementFareList")
    public RestResult getAgreementFareList(@Validated @RequestBody GetAgreementFareListValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return transportService.getAgreementFareList(req.getId(), req.getGid(), req.getAid(), req.getSid(), req.getType(),
                req.getPage(), req.getLimit(), ReviewType.valueOf(req.getReview()), CompleteType.valueOf(req.getComplete()), start, end);
    }

    @PostMapping("/getOfflineFareList")
    public RestResult getOfflineFareList(@Validated @RequestBody GetOfflineFareListValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return transportService.getOfflineFareList(req.getId(), req.getGid(), req.getAid(), req.getSid(), req.getType(),
                req.getPage(), req.getLimit(), ReviewType.valueOf(req.getReview()), CompleteType.valueOf(req.getComplete()), start, end);
    }

    @PostMapping("/getProductFareList")
    public RestResult getProductFareList(@Validated @RequestBody GetProductFareListValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return transportService.getProductFareList(req.getId(), req.getGid(), req.getSid(), req.getType(), req.getPage(),
                req.getLimit(), ReviewType.valueOf(req.getReview()), CompleteType.valueOf(req.getComplete()), start, end);
    }

    @PostMapping("/getPurchaseFareList")
    public RestResult getPurchaseFareList(@Validated @RequestBody GetPurchaseFareListValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return transportService.getPurchaseFareList(req.getId(), req.getGid(), req.getSid(), req.getSupplier(), req.getType(),
                req.getPage(), req.getLimit(), ReviewType.valueOf(req.getReview()), CompleteType.valueOf(req.getComplete()), start, end);
    }

    @PostMapping("/getStorageFareList")
    public RestResult getStorageFareList(@Validated @RequestBody GetStorageFareListValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return transportService.getStorageFareList(req.getId(), req.getGid(), req.getSid(), req.getType(), req.getPage(),
                req.getLimit(), ReviewType.valueOf(req.getReview()), CompleteType.valueOf(req.getComplete()), start, end);
    }
}
