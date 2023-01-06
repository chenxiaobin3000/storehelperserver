package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.commodity.AddCommodityValid;
import com.cxb.storehelperserver.controller.request.commodity.DelCommodityValid;
import com.cxb.storehelperserver.controller.request.commodity.GetGroupCommodityValid;
import com.cxb.storehelperserver.controller.request.commodity.SetCommodityValid;
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
        commodity.setAtid(req.getAtid());
        commodity.setCid(req.getCid());
        commodity.setPrice(req.getPrice());
        commodity.setRemark(req.getRemark());
        return commodityService.addCommodity(req.getId(), commodity, req.getAttributes());
    }

    @PostMapping("/setCommodity")
    public RestResult setCommodity(@Validated @RequestBody SetCommodityValid req) {
        TCommodity commodity = new TCommodity();
        commodity.setId(req.getCommid());
        commodity.setGid(req.getGid());
        commodity.setCode(req.getCode());
        commodity.setName(req.getName());
        commodity.setAtid(req.getAtid());
        commodity.setCid(req.getCid());
        commodity.setPrice(req.getPrice());
        commodity.setRemark(req.getRemark());
        return commodityService.setCommodity(req.getId(), commodity, req.getAttributes());
    }

    @PostMapping("/delCommodity")
    public RestResult delCommodity(@Validated @RequestBody DelCommodityValid req) {
        return commodityService.delCommodity(req.getId(), req.getCid());
    }

    @PostMapping("/getGroupCommodity")
    public RestResult getGroupCommodity(@Validated @RequestBody GetGroupCommodityValid req) {
        return commodityService.getGroupCommodity(req.getId());
    }
}
