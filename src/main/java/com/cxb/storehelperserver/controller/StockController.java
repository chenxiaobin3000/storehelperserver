package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.stock.CountStockValid;
import com.cxb.storehelperserver.controller.request.stock.GetStockCommodityValid;
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
        return stockService.getStockCommodity(req.getId(), req.getSid(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockHalfgood")
    public RestResult getStockHalfgood(@Validated @RequestBody GetStockCommodityValid req) {
        return stockService.getStockHalfgood(req.getId(), req.getSid(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockOriginal")
    public RestResult getStockOriginal(@Validated @RequestBody GetStockCommodityValid req) {
        return stockService.getStockOriginal(req.getId(), req.getSid(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockStandard")
    public RestResult getStockStandard(@Validated @RequestBody GetStockCommodityValid req) {
        return stockService.getStockStandard(req.getId(), req.getSid(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStockDestroy")
    public RestResult getStockDestroy(@Validated @RequestBody GetStockCommodityValid req) {
        return stockService.getStockDestroy(req.getId(), req.getSid(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/countStock")
    public RestResult countStock(@Validated @RequestBody CountStockValid req) {
        return stockService.countStock(req.getId(), req.getSid(), req.getDate());
    }
}
