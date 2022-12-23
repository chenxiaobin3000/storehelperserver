package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.group.*;
import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.service.GroupService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * desc: 公司接口
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@RestController
@RequestMapping("/api/group")
public class GroupController {
    @Resource
    private GroupService groupService;

    @PostMapping("/addGroup")
    public RestResult addGroup(@Validated @RequestBody AddGroupValid req) {
        TGroup group = new TGroup();
        group.setContact(req.getContact());
        group.setName(req.getName());
        group.setAddress(req.getAddress());
        return groupService.addGroup(group);
    }

    @PostMapping("/setGroup")
    public RestResult setGroup(@Validated @RequestBody SetGroupValid req) {
        TGroup group = new TGroup();
        group.setContact(req.getContact());
        group.setName(req.getName());
        group.setAddress(req.getAddress());
        return groupService.setGroup(group);
    }

    @PostMapping("/delGroup")
    public RestResult delGroup(@Validated @RequestBody DelGroupValid req) {
        return groupService.delGroup(req.getId());
    }

    @PostMapping("/getUserGroup")
    public RestResult getUserGroup(@Validated @RequestBody GetUserGroupValid req) {
        return groupService.getUserGroup(1);
    }

    @PostMapping("/setUserGroup")
    public RestResult setUserGroup(@Validated @RequestBody SetUserGroupValid req) {
        return groupService.setUserGroup(1, 1);
    }
}
