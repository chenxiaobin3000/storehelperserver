package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.cloud.*;
import com.cxb.storehelperserver.model.TCloud;
import com.cxb.storehelperserver.service.CloudMgrService;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 云仓接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/cloud")
public class CloudController {
    @Resource
    private CloudMgrService cloudMgrService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/addCloud")
    public RestResult addCloud(@Validated @RequestBody AddCloudValid req) {
        TCloud cloud = new TCloud();
        cloud.setGid(req.getGid());
        cloud.setArea(Long.valueOf(req.getArea()));
        cloud.setContact(req.getContact());
        cloud.setName(req.getName());
        cloud.setAddress(req.getAddress());
        return cloudMgrService.addCloud(req.getId(), cloud);
    }

    @PostMapping("/setCloud")
    public RestResult setCloud(@Validated @RequestBody SetCloudValid req) {
        TCloud cloud = new TCloud();
        cloud.setId(req.getSid());
        cloud.setGid(req.getGid());
        cloud.setArea(Long.valueOf(req.getArea()));
        cloud.setContact(req.getContact());
        cloud.setName(req.getName());
        cloud.setAddress(req.getAddress());
        return cloudMgrService.setCloud(req.getId(), cloud);
    }

    @PostMapping("/delCloud")
    public RestResult delCloud(@Validated @RequestBody DelCloudValid req) {
        return cloudMgrService.delCloud(req.getId(), req.getGid(), req.getSid());
    }

    @PostMapping("/getGroupCloud")
    public RestResult getGroupCloud(@Validated @RequestBody GetGroupCloudValid req) {
        return cloudMgrService.getGroupCloud(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getGroupAllCloud")
    public RestResult getGroupAllCloud(@Validated @RequestBody GetGroupAllCloudValid req) {
        return cloudMgrService.getGroupAllCloud(req.getId());
    }
}
