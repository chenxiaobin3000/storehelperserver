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
        group.setArea(req.getArea());
        group.setContact(req.getContact());
        group.setName(req.getName());
        group.setAddress(req.getAddress());
        return groupService.addGroup(group);
    }

    @PostMapping("/setGroup")
    public RestResult setGroup(@Validated @RequestBody SetGroupValid req) {
        TGroup group = new TGroup();
        group.setId(req.getGid());
        group.setArea(req.getArea());
        group.setContact(req.getContact());
        group.setName(req.getName());
        group.setAddress(req.getAddress());
        return groupService.setGroup(group);
    }

    @PostMapping("/delGroup")
    public RestResult delGroup(@Validated @RequestBody DelGroupValid req) {
        return groupService.delGroup(req.getId(), req.getGid());
    }

    @PostMapping("/getGroupList")
    public RestResult getGroupList(@Validated @RequestBody GetGroupListValid req) {
        return groupService.getGroupList(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getUserGroup")
    public RestResult getUserGroup(@Validated @RequestBody GetUserGroupValid req) {
        return groupService.getUserGroup(1);
    }

    @PostMapping("/setUserGroup")
    public RestResult setUserGroup(@Validated @RequestBody SetUserGroupValid req) {
        return groupService.setUserGroup(req.getId(), req.getUid(), req.getGid());
    }

    @PostMapping("/setUserGroupAdmin")
    public RestResult setUserGroupAdmin(@Validated @RequestBody SetUserGroupValid req) {
        return groupService.setUserGroupAdmin(req.getId(), req.getUid(), req.getGid());
    }
}
