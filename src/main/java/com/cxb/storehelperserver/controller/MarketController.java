package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.market.*;
import com.cxb.storehelperserver.service.MarketService;
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

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/setMarketCommodity")
    public RestResult setMarketCommodity(@Validated @RequestBody SetMarketCommodityValid req) {
        return marketService.setMarketCommodity(req.getId(), req.getGid(), req.getMid(), req.getCid(), req.getName(), req.getPrice());
    }

    @PostMapping("/delMarketCommodity")
    public RestResult delMarketCommodity(@Validated @RequestBody DelMarketCommodityValid req) {
        return marketService.delMarketCommodity(req.getId(), req.getGid(), req.getMid(), req.getCid());
    }

    @PostMapping("/getMarketCommodity")
    public RestResult getMarketCommodity(@Validated @RequestBody GetMarketCommodityValid req) {
        return marketService.getMarketCommodity(req.getId(), req.getPage(), req.getLimit(), req.getMid(), req.getSearch());
    }

    @PostMapping("/setMarketStandard")
    public RestResult setMarketStandard(@Validated @RequestBody SetMarketStandardValid req) {
        return marketService.setMarketStandard(req.getId(), req.getGid(), req.getMid(), req.getCid(), req.getName(), req.getPrice());
    }

    @PostMapping("/delMarketStandard")
    public RestResult delMarketStandard(@Validated @RequestBody DelMarketStandardValid req) {
        return marketService.delMarketStandard(req.getId(), req.getGid(), req.getMid(), req.getCid());
    }

    @PostMapping("/getMarketStandard")
    public RestResult getMarketStandard(@Validated @RequestBody GetMarketStandardValid req) {
        return marketService.getMarketStandard(req.getId(), req.getPage(), req.getLimit(), req.getMid(), req.getSearch());
    }

    @PostMapping("/setMarketCommDetail")
    public RestResult setMarketCommDetail(@Validated @RequestBody SetMarketCommodityDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return marketService.setMarketCommDetail(req.getId(), req.getGid(), req.getMid(), req.getCid(), req.getValue(), req.getPrice(), date);
    }

    @PostMapping("/delMarketCommDetail")
    public RestResult delMarketCommDetail(@Validated @RequestBody DelMarketCommodityDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return marketService.delMarketCommDetail(req.getId(), req.getGid(), req.getMid(), req.getCid(), date);
    }

    @PostMapping("/getMarketCommDetail")
    public RestResult getMarketCommDetail(@Validated @RequestBody GetMarketCommodityDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return marketService.getMarketCommDetail(req.getId(), req.getPage(), req.getLimit(), req.getMid(), date, req.getSearch());
    }

    @PostMapping("/setMarketStanDetail")
    public RestResult setMarketStanDetail(@Validated @RequestBody SetMarketCommodityDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return marketService.setMarketStanDetail(req.getId(), req.getGid(), req.getMid(), req.getCid(), req.getValue(), req.getPrice(), date);
    }

    @PostMapping("/delMarketStanDetail")
    public RestResult delMarketStanDetail(@Validated @RequestBody DelMarketCommodityDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return marketService.delMarketStanDetail(req.getId(), req.getGid(), req.getMid(), req.getCid(), date);
    }

    @PostMapping("/getMarketStanDetail")
    public RestResult getMarketStanDetail(@Validated @RequestBody GetMarketCommodityDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return marketService.getMarketStanDetail(req.getId(), req.getPage(), req.getLimit(), req.getMid(), date, req.getSearch());
    }
}
