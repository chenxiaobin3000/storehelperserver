package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.stock.*;
import com.cxb.storehelperserver.service.StockCloudService;
import com.cxb.storehelperserver.service.StockService;
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
    private StockService stockService;

    @Resource
    private StockCloudService stockCloudService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/getStockList")
    public RestResult getStockList(@Validated @RequestBody GetStockListValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockList(req.getId(), req.getSid(), req.getPage(), req.getLimit(), date, req.getSearch());
    }

    @PostMapping("/getTodayStockList")
    public RestResult getTodayStockList(@Validated @RequestBody GetTodayStockListValid req) {
        return stockService.getTodayStockList(req.getId(), req.getSid(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockDay")
    public RestResult getStockDay(@Validated @RequestBody GetStockDayValid req) {
        return stockService.getStockDay(req.getId(), req.getGid(), req.getSid());
    }

    @PostMapping("/getStockWeek")
    public RestResult getStockWeek(@Validated @RequestBody GetStockWeekValid req) {
        return stockService.getStockWeek(req.getId(), req.getGid(), req.getSid());
    }

    @PostMapping("/getCloudList")
    public RestResult getCloudList(@Validated @RequestBody GetCloudListValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockCloudService.getStockList(req.getId(), req.getAid(), req.getPage(), req.getLimit(), date, req.getSearch());
    }

    @PostMapping("/getTodayCloudList")
    public RestResult getTodayCloudList(@Validated @RequestBody GetTodayCloudListValid req) {
        return stockCloudService.getTodayStockList(req.getId(), req.getAid(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getCloudDay")
    public RestResult getCloudDay(@Validated @RequestBody GetCloudDayValid req) {
        return stockCloudService.getStockDay(req.getId(), req.getGid(), req.getAid());
    }

    @PostMapping("/getCloudWeek")
    public RestResult getCloudWeek(@Validated @RequestBody GetCloudWeekValid req) {
        return stockCloudService.getStockWeek(req.getId(), req.getGid(), req.getAid());
    }
}
