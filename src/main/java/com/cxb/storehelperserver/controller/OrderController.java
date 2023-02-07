package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.order.*;
import com.cxb.storehelperserver.service.OrderService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

    @PostMapping("/getAgreementOrder")
    public RestResult getAgreementOrder(@Validated @RequestBody GetAgreementOrderValid req) {
        return orderService.getAgreementOrder(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getProductOrder")
    public RestResult getProductOrder(@Validated @RequestBody GetProductOrderValid req) {
        return orderService.getProductOrder(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStorageOrder")
    public RestResult getStorageOrder(@Validated @RequestBody GetStorageOrderValid req) {
        return orderService.getStorageOrder(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
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