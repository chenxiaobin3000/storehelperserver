package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.halfgood.*;
import com.cxb.storehelperserver.model.THalfgood;
import com.cxb.storehelperserver.service.HalfgoodService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 半成品接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/halfgood")
public class HalfgoodController {
    @Resource
    private HalfgoodService halfgoodService;

    @PostMapping("/addHalfgood")
    public RestResult addHalfgood(@Validated @RequestBody AddHalfgoodValid req) {
        THalfgood halfgood = new THalfgood();
        halfgood.setGid(req.getGid());
        halfgood.setCode(req.getCode());
        halfgood.setName(req.getName());
        halfgood.setCid(req.getCid());
        halfgood.setRemark(req.getRemark());
        return halfgoodService.addHalfgood(req.getId(), halfgood, req.getAttrs());
    }

    @PostMapping("/setHalfgood")
    public RestResult setHalfgood(@Validated @RequestBody SetHalfgoodValid req) {
        THalfgood halfgood = new THalfgood();
        halfgood.setId(req.getHid());
        halfgood.setGid(req.getGid());
        halfgood.setCode(req.getCode());
        halfgood.setName(req.getName());
        halfgood.setCid(req.getCid());
        halfgood.setRemark(req.getRemark());
        return halfgoodService.setHalfgood(req.getId(), halfgood, req.getAttrs());
    }

    @PostMapping("/delHalfgood")
    public RestResult delHalfgood(@Validated @RequestBody DelHalfgoodValid req) {
        return halfgoodService.delHalfgood(req.getId(), req.getHid());
    }

    @PostMapping("/getHalfgood")
    public RestResult getHalfgood(@Validated @RequestBody GetHalfgoodValid req) {
        return halfgoodService.getHalfgood(req.getId(), req.getHid());
    }

    @PostMapping("/getGroupHalfgood")
    public RestResult getGroupHalfgood(@Validated @RequestBody GetGroupHalfgoodValid req) {
        return halfgoodService.getGroupHalfgood(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStorageHalfgood")
    public RestResult getStorageHalfgood(@Validated @RequestBody GetStorageHalfgoodValid req) {
        return halfgoodService.getStorageHalfgood(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/setHalfgoodOriginal")
    public RestResult setHalfgoodOriginal(@Validated @RequestBody SetHalfgoodOriginalValid req) {
        return halfgoodService.setHalfgoodOriginal(req.getId(), req.getGid(), req.getHid(), req.getOid());
    }

    @PostMapping("/setHalfgoodStorage")
    public RestResult setHalfgoodStorage(@Validated @RequestBody SetHalfgoodStorageValid req) {
        return halfgoodService.setHalfgoodStorage(req.getId(), req.getGid(), req.getCid(), req.getSids());
    }
}
