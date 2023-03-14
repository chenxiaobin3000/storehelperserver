package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.stock.*;
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
    private DateUtil dateUtil;

    @PostMapping("/getStockList")
    public RestResult getStockList(@Validated @RequestBody GetStockListValid req) {
        return storageStockService.getStockList(req.getId(), req.getSid(), req.getCtype(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockDay")
    public RestResult getStockDay(@Validated @RequestBody GetStockDayValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return storageStockService.getStockDay(req.getId(), req.getSid(), req.getCtype(), date, req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockWeek")
    public RestResult getStockWeek(@Validated @RequestBody GetStockWeekValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return storageStockService.getStockWeek(req.getId(), req.getSid(), req.getCtype(), date, req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getCloudDay")
    public RestResult getCloudDay(@Validated @RequestBody GetCloudDayValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return null;
        // return stockService.getCloudDay(req.getId(), req.getSid(), req.getCtype(), date, req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getCloudWeek")
    public RestResult getCloudWeek(@Validated @RequestBody GetCloudWeekValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return null;
        // return stockService.getCloudWeek(req.getId(), req.getSid(), req.getCtype(), date, req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/countStock")
    public RestResult countStock(@Validated @RequestBody CountStockValid req) {
        return storageStockService.countStock(req.getId(), req.getGid());
    }
}
