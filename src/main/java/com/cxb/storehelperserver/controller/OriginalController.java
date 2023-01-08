package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.original.AddOriginalValid;
import com.cxb.storehelperserver.controller.request.original.DelOriginalValid;
import com.cxb.storehelperserver.controller.request.original.GetGroupOriginalValid;
import com.cxb.storehelperserver.controller.request.original.SetOriginalValid;
import com.cxb.storehelperserver.model.TOriginal;
import com.cxb.storehelperserver.service.OriginalService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 原料接口
 * auth: cxb
 * date: 2023/1/6
 */
@Slf4j
@RestController
@RequestMapping("/api/original")
public class OriginalController {
    @Resource
    private OriginalService originalService;

    @PostMapping("/addOriginal")
    public RestResult addOriginal(@Validated @RequestBody AddOriginalValid req) {
        TOriginal original = new TOriginal();
        original.setGid(req.getGid());
        original.setCode(req.getCode());
        original.setName(req.getName());
        original.setAtid(req.getAtid());
        original.setCid(req.getCid());
        original.setPrice(req.getPrice());
        original.setRemark(req.getRemark());
        return originalService.addOriginal(req.getId(), original, req.getAttrs());
    }

    @PostMapping("/setOriginal")
    public RestResult setOriginal(@Validated @RequestBody SetOriginalValid req) {
        TOriginal original = new TOriginal();
        original.setId(req.getCommid());
        original.setGid(req.getGid());
        original.setCode(req.getCode());
        original.setName(req.getName());
        original.setAtid(req.getAtid());
        original.setCid(req.getCid());
        original.setPrice(req.getPrice());
        original.setRemark(req.getRemark());
        return originalService.setOriginal(req.getId(), original, req.getAttrs());
    }

    @PostMapping("/delOriginal")
    public RestResult delOriginal(@Validated @RequestBody DelOriginalValid req) {
        return originalService.delOriginal(req.getId(), req.getCid());
    }

    @PostMapping("/getGroupOriginal")
    public RestResult getGroupOriginal(@Validated @RequestBody GetGroupOriginalValid req) {
        return originalService.getGroupOriginal(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
