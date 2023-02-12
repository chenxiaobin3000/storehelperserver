package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.stock.*;
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
    private DateUtil dateUtil;

    @PostMapping("/getStockCommodity")
    public RestResult getStockCommodity(@Validated @RequestBody GetStockCommodityValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockCommodity(req.getId(), req.getSid(), date, req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockHalfgood")
    public RestResult getStockHalfgood(@Validated @RequestBody GetStockHalfgoodValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockHalfgood(req.getId(), req.getSid(), date, req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockOriginal")
    public RestResult getStockOriginal(@Validated @RequestBody GetStockOriginalValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockOriginal(req.getId(), req.getSid(), date, req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockStandard")
    public RestResult getStockStandard(@Validated @RequestBody GetStockStandardValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockStandard(req.getId(), req.getSid(), date, req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockDestroy")
    public RestResult getStockDestroy(@Validated @RequestBody GetStockDestoryValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockDestroy(req.getId(), req.getSid(), date, req.getPage(), req.getLimit(), req.getSearch());
    }

    // TODO 财务，人工，包装，物流费，移除商品价格，商家列表配置销售平台,库存销量饼图
    @PostMapping("/countStock")
    public RestResult countStock(@Validated @RequestBody CountStockValid req) {
        return stockService.countStock(req.getId(), req.getGid());
    }
}
