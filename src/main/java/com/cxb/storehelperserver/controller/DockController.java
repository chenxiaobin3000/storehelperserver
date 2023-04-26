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
        return dockService.addMarketAccount(req.getId(), req.getGid(), req.getMid(), req.getAccount(), req.getRemark());
    }

    @PostMapping("/setMarketAccount")
    public RestResult setMarketAccount(@Validated @RequestBody SetMarketAccountValid req) {
        return dockService.setMarketAccount(req.getId(), req.getGid(), req.getMid(), req.getAid(), req.getAccount(), req.getRemark());
    }

    @PostMapping("/delMarketAccount")
    public RestResult delMarketAccount(@Validated @RequestBody DelMarketAccountValid req) {
        return dockService.delMarketAccount(req.getId(), req.getGid(), req.getMid(), req.getAid());
    }

    @PostMapping("/getMarketAccountList")
    public RestResult getMarketAccountList(@Validated @RequestBody GetMarketAccountListValid req) {
        return dockService.getMarketAccountList(req.getId(), req.getGid(), req.getMid(), req.getPage(), req.getLimit());
    }

    @PostMapping("/getMarketAllAccount")
    public RestResult getMarketAllAccount(@Validated @RequestBody GetMarketAllAccountValid req) {
        return dockService.getMarketAllAccount(req.getId(), req.getGid());
    }

    @PostMapping("/getMarketStorageAccount")
    public RestResult getMarketStorageAccount(@Validated @RequestBody GetMarketStorageAccountValid req) {
        return dockService.getMarketStorageAccount(req.getId(), req.getGid(), req.getCid());
    }

    @PostMapping("/getMarketSubAccount")
    public RestResult getMarketSubAccount(@Validated @RequestBody GetMarketSubAccountValid req) {
        return dockService.getMarketSubAccount(req.getId(), req.getGid(), req.getAid());
    }

    @PostMapping("/addMarketMany")
    public RestResult addMarketMany(@Validated @RequestBody AddMarketManyValid req) {
        return dockService.addMarketMany(req.getId(), req.getGid(), req.getMid(), req.getAid(), req.getAccount(), req.getRemark());
    }

    @PostMapping("/setMarketMany")
    public RestResult setMarketMany(@Validated @RequestBody SetMarketManyValid req) {
        return dockService.setMarketMany(req.getId(), req.getGid(), req.getMid(), req.getAid(), req.getSub(), req.getAccount(), req.getRemark());
    }

    @PostMapping("/delMarketMany")
    public RestResult delMarketMany(@Validated @RequestBody DelMarketManyValid req) {
        return dockService.delMarketMany(req.getId(), req.getGid(), req.getSub());
    }

    @PostMapping("/getMarketManyList")
    public RestResult getMarketManyList(@Validated @RequestBody GetMarketManyListValid req) {
        return dockService.getMarketManyList(req.getId(), req.getGid(), req.getPage(), req.getLimit());
    }

    @PostMapping("/setMarketStorage")
    public RestResult setMarketStorage(@Validated @RequestBody SetMarketStorageValid req) {
        return dockService.setMarketStorage(req.getId(), req.getGid(), req.getAid(), req.getCid());
    }

    @PostMapping("/delMarketStorage")
    public RestResult delMarketStorage(@Validated @RequestBody DelMarketStorageValid req) {
        return dockService.delMarketStorage(req.getId(), req.getGid(), req.getCid());
    }

    @PostMapping("/getMarketStorageList")
    public RestResult getMarketStorageList(@Validated @RequestBody GetMarketStorageListValid req) {
        return dockService.getMarketStorageList(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getAccountStorage")
    public RestResult getAccountStorage(@Validated @RequestBody GetAccountStorageValid req) {
        return dockService.getAccountStorage(req.getId(), req.getGid(), req.getAid());
    }
}
