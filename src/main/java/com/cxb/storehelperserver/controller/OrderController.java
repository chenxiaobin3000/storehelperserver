package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.order.*;
import com.cxb.storehelperserver.service.OrderService;
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
import static com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 仓库接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Resource
    private OrderService orderService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/addOrderRemark")
    public RestResult addOrderRemark(@Validated @RequestBody AddOrderRemarkValid req) {
        return orderService.addOrderRemark(req.getId(), BusinessType.valueOf(req.getOtype()), req.getOid(), req.getRemark());
    }

    @PostMapping("/delOrderRemark")
    public RestResult delOrderRemark(@Validated @RequestBody DelOrderRemarkValid req) {
        return orderService.delOrderRemark(req.getId(), BusinessType.valueOf(req.getOtype()), req.getOid(), req.getRid());
    }

    @PostMapping("/getAgreementOrder")
    public RestResult getAgreementOrder(@Validated @RequestBody GetAgreementOrderValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return orderService.getAgreementOrder(req.getId(), req.getAid(), req.getType(), req.getPage(), req.getLimit(),
                ReviewType.valueOf(req.getReview()), CompleteType.valueOf(req.getComplete()), start, end, req.getSearch());
    }

    @PostMapping("/getOfflineOrder")
    public RestResult getOfflineOrder(@Validated @RequestBody GetOfflineOrderValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return orderService.getOfflineOrder(req.getId(), req.getAid(), req.getType(), req.getPage(), req.getLimit(),
                ReviewType.valueOf(req.getReview()), CompleteType.valueOf(req.getComplete()), start, end, req.getSearch());
    }

    @PostMapping("/getProductOrder")
    public RestResult getProductOrder(@Validated @RequestBody GetProductOrderValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return orderService.getProductOrder(req.getId(), req.getType(), req.getPage(), req.getLimit(), ReviewType.valueOf(req.getReview()), start, end, req.getSearch());
    }

    @PostMapping("/getPurchaseOrder")
    public RestResult getPurchaseOrder(@Validated @RequestBody GetPurchaseOrderValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return orderService.getPurchaseOrder(req.getId(), req.getType(), req.getPage(), req.getLimit(),
                ReviewType.valueOf(req.getReview()), CompleteType.valueOf(req.getComplete()), start, end, req.getSearch());
    }

    @PostMapping("/getStorageOrder")
    public RestResult getStorageOrder(@Validated @RequestBody GetStorageOrderValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return orderService.getStorageOrder(req.getId(), req.getType(), req.getPage(), req.getLimit(), ReviewType.valueOf(req.getReview()), start, end, req.getSearch());
    }

    @PostMapping("/getSaleOrder")
    public RestResult getSaleOrder(@Validated @RequestBody GetSaleOrderValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("订单日期转换失败");
        }
        return orderService.getSaleOrder(req.getId(), req.getType(), req.getPage(), req.getLimit(), ReviewType.valueOf(req.getReview()), start, end, req.getSearch());
    }

    @PostMapping("/getMyWait")
    public RestResult getMyWait(@Validated @RequestBody GetMyWaitValid req) {
        return orderService.getMyWait(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getMyCheck")
    public RestResult getMyCheck(@Validated @RequestBody GetMyCheckValid req) {
        return orderService.getMyCheck(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getMyComplete")
    public RestResult getMyComplete(@Validated @RequestBody GetMyCompleteValid req) {
        return orderService.getMyComplete(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
