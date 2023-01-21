package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.destroy.*;
import com.cxb.storehelperserver.model.TDestroy;
import com.cxb.storehelperserver.service.DestroyService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 废品接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/destroy")
public class DestroyController {
    @Resource
    private DestroyService destroyService;

    @PostMapping("/addDestroy")
    public RestResult addDestroy(@Validated @RequestBody AddDestroyValid req) {
        TDestroy destroy = new TDestroy();
        destroy.setGid(req.getGid());
        destroy.setCode(req.getCode());
        destroy.setName(req.getName());
        destroy.setCid(req.getCid());
        destroy.setPrice(req.getPrice());
        destroy.setUnit(req.getUnit());
        destroy.setRemark(req.getRemark());
        return destroyService.addDestroy(req.getId(), destroy, req.getAttrs());
    }

    @PostMapping("/setDestroy")
    public RestResult setDestroy(@Validated @RequestBody SetDestroyValid req) {
        TDestroy destroy = new TDestroy();
        destroy.setId(req.getDestid());
        destroy.setGid(req.getGid());
        destroy.setCode(req.getCode());
        destroy.setName(req.getName());
        destroy.setCid(req.getCid());
        destroy.setPrice(req.getPrice());
        destroy.setUnit(req.getUnit());
        destroy.setRemark(req.getRemark());
        return destroyService.setDestroy(req.getId(), destroy, req.getAttrs());
    }

    @PostMapping("/delDestroy")
    public RestResult delDestroy(@Validated @RequestBody DelDestroyValid req) {
        return destroyService.delDestroy(req.getId(), req.getDid());
    }

    @PostMapping("/getDestroy")
    public RestResult getDestroy(@Validated @RequestBody GetDestroyValid req) {
        return destroyService.getDestroy(req.getId(), req.getDid());
    }

    @PostMapping("/getGroupDestroy")
    public RestResult getGroupDestroy(@Validated @RequestBody GetGroupDestroyValid req) {
        return destroyService.getGroupDestroy(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/setDestroyOriginal")
    public RestResult setDestroyOriginal(@Validated @RequestBody SetDestroyOriginalValid req) {
        return destroyService.setDestroyOriginal(req.getId(), req.getGid(), req.getDid(), req.getOid());
    }
}
