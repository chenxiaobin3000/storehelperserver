package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.dock.*;
import com.cxb.storehelperserver.service.DockService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 市场对接接口
 * auth: cxb
 * date: 2023/2/7
 */
@Slf4j
@RestController
@RequestMapping("/api/dock")
public class DockController {
    @Resource
    private DockService dockService;

    @PostMapping("/addMarketAccount")
    public RestResult addMarketAccount(@Validated @RequestBody AddMarketAccountValid req) {
        return dockService.addMarketAccount(req.getId(), req.getGid(), req.getMid(), req.getAccount());
    }

    @PostMapping("/setMarketAccount")
    public RestResult setMarketAccount(@Validated @RequestBody SetMarketAccountValid req) {
        return dockService.setMarketAccount(req.getId(), req.getGid(), req.getMid(), req.getAid(), req.getAccount());
    }

    @PostMapping("/delMarketAccount")
    public RestResult delMarketAccount(@Validated @RequestBody DelMarketAccountValid req) {
        return dockService.delMarketAccount(req.getId(), req.getGid(), req.getMid(), req.getAid());
    }

    @PostMapping("/getMarketAccountList")
    public RestResult getMarketAccountList(@Validated @RequestBody GetMarketAccountListValid req) {
        return dockService.getMarketAccountList(req.getId(), req.getGid(), req.getMid(), req.getPage(), req.getLimit());
    }

    @PostMapping("/setMarketCloud")
    public RestResult setMarketCloud(@Validated @RequestBody SetMarketCloudValid req) {
        return dockService.setMarketCloud(req.getId(), req.getGid(), req.getMid(), req.getCid(), req.getAccount());
    }

    @PostMapping("/delMarketCloud")
    public RestResult delMarketCloud(@Validated @RequestBody DelMarketCloudValid req) {
        return dockService.delMarketCloud(req.getId(), req.getGid(), req.getMid(), req.getCid());
    }

    @PostMapping("/getMarketCloudList")
    public RestResult getMarketCloudList(@Validated @RequestBody GetMarketCloudListValid req) {
        return dockService.getMarketCloudList(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
