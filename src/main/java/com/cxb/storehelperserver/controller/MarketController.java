package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.market.*;
import com.cxb.storehelperserver.model.TMarketCommodityDetail;
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

import static com.cxb.storehelperserver.util.TypeDefine.ReportCycleType;

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

    @PostMapping("/addMarketAccount")
    public RestResult addMarketAccount(@Validated @RequestBody AddMarketAccountValid req) {
        return marketService.addMarketAccount(req.getId(), req.getGid(), req.getMid(), req.getAccount(), req.getRemark());
    }

    @PostMapping("/setMarketAccount")
    public RestResult setMarketAccount(@Validated @RequestBody SetMarketAccountValid req) {
        return marketService.setMarketAccount(req.getId(), req.getGid(), req.getMid(), req.getAid(), req.getAccount(), req.getRemark());
    }

    @PostMapping("/delMarketAccount")
    public RestResult delMarketAccount(@Validated @RequestBody DelMarketAccountValid req) {
        return marketService.delMarketAccount(req.getId(), req.getGid(), req.getMid(), req.getAid());
    }

    @PostMapping("/getMarketAccountList")
    public RestResult getMarketAccountList(@Validated @RequestBody GetMarketAccountListValid req) {
        return marketService.getMarketAccountList(req.getId(), req.getGid(), req.getMid(), req.getPage(), req.getLimit());
    }

    @PostMapping("/getMarketAllAccount")
    public RestResult getMarketAllAccount(@Validated @RequestBody GetMarketAllAccountValid req) {
        return marketService.getMarketAllAccount(req.getId(), req.getGid());
    }

    @PostMapping("/setMarketCommodity")
    public RestResult setMarketCommodity(@Validated @RequestBody SetMarketCommodityValid req) {
        return marketService.setMarketCommodity(req.getId(), req.getGid(), req.getAid(), req.getCid(), req.getCode(), req.getName(), req.getRemark(), req.getPrice());
    }

    @PostMapping("/delMarketCommodity")
    public RestResult delMarketCommodity(@Validated @RequestBody DelMarketCommodityValid req) {
        return marketService.delMarketCommodity(req.getId(), req.getGid(), req.getAid(), req.getCid());
    }

    @PostMapping("/getMarketCommodity")
    public RestResult getMarketCommodity(@Validated @RequestBody GetMarketCommodityValid req) {
        return marketService.getMarketCommodity(req.getId(), req.getGid(), req.getAid(), req.getCid());
    }

    @PostMapping("/getMarketCommodityList")
    public RestResult getMarketCommodityList(@Validated @RequestBody GetMarketCommodityListValid req) {
        return marketService.getMarketCommodityList(req.getId(), req.getGid(), req.getPage(), req.getLimit(), req.getAid(), req.getSearch());
    }

    @PostMapping("/setMarketCommList")
    public RestResult setMarketCommList(@Validated @RequestBody SetMarketCommodityListValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return marketService.setMarketCommodityList(req.getId(), req.getGid(), req.getSid(), req.getAid(), date, req.getCommoditys(), req.getPrices(), req.getValues());
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
        TMarketCommodityDetail detail = new TMarketCommodityDetail();
        detail.setId(0 == req.getDid() ? null : req.getDid());
        detail.setGid(req.getGid());
        detail.setAid(req.getAid());
        detail.setCid(req.getCid());
        detail.setValue(req.getValue());
        detail.setPrice(req.getPrice());
        detail.setCdate(date);
        return marketService.setMarketCommodityDetail(req.getId(), req.getGid(), detail);
    }

    @PostMapping("/delMarketCommDetail")
    public RestResult delMarketCommDetail(@Validated @RequestBody DelMarketCommodityDetailValid req) {
        return marketService.delMarketCommodityDetail(req.getId(), req.getGid(), req.getDid());
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
        return marketService.getMarketCommodityDetail(req.getId(), req.getGid(), req.getPage(), req.getLimit(), req.getSid(), req.getAid(), date, req.getSearch());
    }

    @PostMapping("/getMarketSaleDetail")
    public RestResult getMarketSaleDetail(@Validated @RequestBody GetMarketSaleDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return marketService.getMarketSaleDetail(req.getId(), req.getGid(), req.getAid(), date);
    }

    @PostMapping("/getCommoditySaleInfo")
    public RestResult getCommoditySaleInfo(@Validated @RequestBody GetCommoditySaleInfoValid req) {
        return marketService.getCommoditySaleInfo(req.getId(), req.getPage(), req.getLimit(), req.getMid(), ReportCycleType.valueOf(req.getCycle()), req.getSearch());
    }
}
