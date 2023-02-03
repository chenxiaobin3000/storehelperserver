package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.stock.CountStockDayValid;
import com.cxb.storehelperserver.controller.request.stock.GetStockCommodityValid;
import com.cxb.storehelperserver.service.StockService;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.TypeDefine;
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

import static com.cxb.storehelperserver.util.TypeDefine.ReportCycleType;

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
        return stockService.getStockCommodity(req.getId(), req.getSid(), date, ReportCycleType.valueOf(req.getCycle()), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockHalfgood")
    public RestResult getStockHalfgood(@Validated @RequestBody GetStockCommodityValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockHalfgood(req.getId(), req.getSid(), date, ReportCycleType.valueOf(req.getCycle()), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockOriginal")
    public RestResult getStockOriginal(@Validated @RequestBody GetStockCommodityValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockOriginal(req.getId(), req.getSid(), date, ReportCycleType.valueOf(req.getCycle()), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockStandard")
    public RestResult getStockStandard(@Validated @RequestBody GetStockCommodityValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockStandard(req.getId(), req.getSid(), date, ReportCycleType.valueOf(req.getCycle()), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockDestroy")
    public RestResult getStockDestroy(@Validated @RequestBody GetStockCommodityValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return stockService.getStockDestroy(req.getId(), req.getSid(), date, ReportCycleType.valueOf(req.getCycle()), req.getPage(), req.getLimit(), req.getSearch());
    }

    // TODO 库存，展示，包装，销售
    @PostMapping("/countStockDay")
    public RestResult countStockDay(@Validated @RequestBody CountStockDayValid req) {
        return stockService.countStockDay(req.getId(), req.getGid());
    }
}
