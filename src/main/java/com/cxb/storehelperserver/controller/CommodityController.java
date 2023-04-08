package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.commodity.*;
import com.cxb.storehelperserver.model.TCommodity;
import com.cxb.storehelperserver.service.CommodityService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 商品接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/commodity")
public class CommodityController {
    @Resource
    private CommodityService commodityService;

    @PostMapping("/addCommodity")
    public RestResult addCommodity(@Validated @RequestBody AddCommodityValid req) {
        TCommodity commodity = new TCommodity();
        commodity.setGid(req.getGid());
        commodity.setCode(req.getCode());
        commodity.setName(req.getName());
        commodity.setCid(req.getCid());
        commodity.setRemark(req.getRemark());
        return commodityService.addCommodity(req.getId(), commodity, req.getAttrs());
    }

    @PostMapping("/setCommodity")
    public RestResult setCommodity(@Validated @RequestBody SetCommodityValid req) {
        TCommodity commodity = new TCommodity();
        commodity.setId(req.getCommid());
        commodity.setGid(req.getGid());
        commodity.setCode(req.getCode());
        commodity.setName(req.getName());
        commodity.setCid(req.getCid());
        commodity.setRemark(req.getRemark());
        return commodityService.setCommodity(req.getId(), commodity, req.getAttrs());
    }

    @PostMapping("/delCommodity")
    public RestResult delCommodity(@Validated @RequestBody DelCommodityValid req) {
        return commodityService.delCommodity(req.getId(), req.getCid());
    }

    @PostMapping("/getCommodity")
    public RestResult getCommodity(@Validated @RequestBody GetCommodityValid req) {
        return commodityService.getCommodity(req.getId(), req.getCid());
    }

    @PostMapping("/getGroupCommodity")
    public RestResult getGroupCommodity(@Validated @RequestBody GetGroupCommodityValid req) {
        return commodityService.getGroupCommodity(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getGroupAllCommodity")
    public RestResult getGroupAllCommodity(@Validated @RequestBody GetGroupAllCommodityValid req) {
        return commodityService.getGroupAllCommodity(req.getId());
    }

    @PostMapping("/setCommodityOriginal")
    public RestResult setCommodityOriginal(@Validated @RequestBody SetCommodityOriginalValid req) {
        return commodityService.setCommodityOriginal(req.getId(), req.getGid(), req.getCid(), req.getOid());
    }

    @PostMapping("/setCommodityStorage")
    public RestResult setCommodityStorage(@Validated @RequestBody SetCommodityStorageValid req) {
        return commodityService.setCommodityStorage(req.getId(), req.getGid(), req.getCid(), req.getSids());
    }
}
