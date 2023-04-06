package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.stock.*;
import com.cxb.storehelperserver.service.CloudStockService;
import com.cxb.storehelperserver.service.StorageStockService;
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

/**
 * desc: 库存接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/stock")
public class StockController {
    @Resource
    private StorageStockService storageStockService;

    @Resource
    private CloudStockService cloudStockService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/getStockList")
    public RestResult getStockList(@Validated @RequestBody GetStockListValid req) {
        return storageStockService.getStockList(req.getId(), req.getSid(), req.getCtype(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockDetail")
    public RestResult getStockList(@Validated @RequestBody GetStockDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return storageStockService.getStockDetail(req.getId(), req.getSid(), req.getCtype(), req.getPage(), req.getLimit(), start, end, req.getSearch());
    }

    @PostMapping("/getStockDay")
    public RestResult getStockDay(@Validated @RequestBody GetStockDayValid req) {
        return storageStockService.getStockDay(req.getId(), req.getGid(), req.getSid(), req.getCtype());
    }

    @PostMapping("/getStockWeek")
    public RestResult getStockWeek(@Validated @RequestBody GetStockWeekValid req) {
        return storageStockService.getStockWeek(req.getId(), req.getGid(), req.getSid(), req.getCtype());
    }

    @PostMapping("/getCloudList")
    public RestResult getCloudList(@Validated @RequestBody GetCloudListValid req) {
        return cloudStockService.getStockList(req.getId(), req.getSid(), req.getCtype(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getCloudDetail")
    public RestResult getCloudDetail(@Validated @RequestBody GetCloudDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date start = null;
        Date end = null;
        try {
            start = simpleDateFormat.parse(req.getDate() + " 00:00:00");
            end = simpleDateFormat.parse(req.getDate() + " 23:59:59");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return cloudStockService.getStockDetail(req.getId(), req.getSid(), req.getCtype(), req.getPage(), req.getLimit(), start, end, req.getSearch());
    }

    @PostMapping("/getCloudDay")
    public RestResult getCloudDay(@Validated @RequestBody GetCloudDayValid req) {
        return cloudStockService.getStockDay(req.getId(), req.getGid(), req.getSid(), req.getCtype());
    }

    @PostMapping("/getCloudWeek")
    public RestResult getCloudWeek(@Validated @RequestBody GetCloudWeekValid req) {
        return cloudStockService.getStockWeek(req.getId(), req.getGid(), req.getSid(), req.getCtype());
    }
}
