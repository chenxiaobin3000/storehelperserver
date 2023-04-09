package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.standard.*;
import com.cxb.storehelperserver.model.TStandard;
import com.cxb.storehelperserver.service.StandardService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 标品接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/standard")
public class StandardController {
    @Resource
    private StandardService standardService;

    @PostMapping("/addStandard")
    public RestResult addStandard(@Validated @RequestBody AddStandardValid req) {
        TStandard standard = new TStandard();
        standard.setGid(req.getGid());
        standard.setCode(req.getCode());
        standard.setName(req.getName());
        standard.setCid(req.getCid());
        standard.setRemark(req.getRemark());
        return standardService.addStandard(req.getId(), standard, req.getAttrs());
    }

    @PostMapping("/setStandard")
    public RestResult setStandard(@Validated @RequestBody SetStandardValid req) {
        TStandard standard = new TStandard();
        standard.setId(req.getSid());
        standard.setGid(req.getGid());
        standard.setCode(req.getCode());
        standard.setName(req.getName());
        standard.setCid(req.getCid());
        standard.setRemark(req.getRemark());
        return standardService.setStandard(req.getId(), standard, req.getAttrs());
    }

    @PostMapping("/delStandard")
    public RestResult delStandard(@Validated @RequestBody DelStandardValid req) {
        return standardService.delStandard(req.getId(), req.getSid());
    }

    @PostMapping("/getStandard")
    public RestResult getStandard(@Validated @RequestBody GetStandardValid req) {
        return standardService.getStandard(req.getId(), req.getSid());
    }

    @PostMapping("/getGroupStandard")
    public RestResult getGroupStandard(@Validated @RequestBody GetGroupStandardValid req) {
        return standardService.getGroupStandard(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getStorageStandard")
    public RestResult getStorageStandard(@Validated @RequestBody GetStorageStandardValid req) {
        return standardService.getStorageStandard(req.getId(), req.getSid(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/setStandardStorage")
    public RestResult setStandardStorage(@Validated @RequestBody SetStandardStorageValid req) {
        return standardService.setStandardStorage(req.getId(), req.getGid(), req.getCid(), req.getSids());
    }
}
