package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.market.*;
import com.cxb.storehelperserver.service.MarketService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 市场接口
 * auth: cxb
 * date: 2023/2/7
 */
@Slf4j
@RestController
@RequestMapping("/api/market")
public class MarketController {
    @Resource
    private MarketService marketService;

    @PostMapping("/setMarketCommodity")
    public RestResult setMarketCommodity(@Validated @RequestBody SetMarketCommodityValid req) {
        return marketService.setMarketCommodity(req.getId(), req.getGid(), req.getMid(), req.getCid(), req.getName(), req.getPrice());
    }

    @PostMapping("/delMarketCommodity")
    public RestResult delMarketCommodity(@Validated @RequestBody DelMarketCommodityValid req) {
        return marketService.delMarketCommodity(req.getId(), req.getGid(), req.getMid(), req.getCid());
    }

    @PostMapping("/addMarketDetail")
    public RestResult addMarketDetail(@Validated @RequestBody AddMarketDetailValid req) {
        return marketService.addMarketDetail(req.getId(), req.getGid(), req.getMid(), req.getCid(), req.getValue(), req.getPrice());
    }

    @PostMapping("/setMarketDetail")
    public RestResult setMarketDetail(@Validated @RequestBody SetMarketDetailValid req) {
        return marketService.setMarketDetail(req.getId(), req.getGid(), req.getDid(), req.getValue(), req.getPrice());
    }

    @PostMapping("/delMarketDetail")
    public RestResult delMarketDetail(@Validated @RequestBody DelMarketDetailValid req) {
        return marketService.delMarketDetail(req.getId(), req.getGid(), req.getDid());
    }

    @PostMapping("/getMarketDetail")
    public RestResult getMarketDetail(@Validated @RequestBody GetMarketDetailValid req) {
        return marketService.getMarketDetail(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
