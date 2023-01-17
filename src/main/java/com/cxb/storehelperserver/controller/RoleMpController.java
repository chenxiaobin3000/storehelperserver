package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.rolemp.*;
import com.cxb.storehelperserver.model.TRoleMp;
import com.cxb.storehelperserver.service.RoleMpService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 小程序角色接口
 * auth: cxb
 * date: 2023/1/17
 */
@Slf4j
@RestController
@RequestMapping("/api/rolemp")
public class RoleMpController {
    @Resource
    private RoleMpService roleMpService;

    @PostMapping("/addRoleMp")
    public RestResult addRoleMp(@Validated @RequestBody AddRoleMpValid req) {
        TRoleMp role = new TRoleMp();
        role.setGid(req.getGid());
        role.setName(req.getName());
        role.setDescription(req.getDesc());
        return roleMpService.addRoleMp(req.getId(), role, req.getPermissions());
    }

    @PostMapping("/setRoleMp")
    public RestResult setRoleMp(@Validated @RequestBody SetRoleMpValid req) {
        TRoleMp role = new TRoleMp();
        role.setId(req.getRid());
        role.setGid(req.getGid());
        role.setName(req.getName());
        role.setDescription(req.getDesc());
        return roleMpService.setRoleMp(req.getId(), role, req.getPermissions());
    }

    @PostMapping("/delRoleMp")
    public RestResult delRoleMp(@Validated @RequestBody DelRoleMpValid req) {
        return roleMpService.delRoleMp(req.getId(), req.getRid());
    }

    @PostMapping("/getRoleMp")
    public RestResult getRoleMp(@Validated @RequestBody GetRoleMpValid req) {
        return roleMpService.getRoleMp(req.getId(), req.getRid());
    }

    @PostMapping("/getRoleMpList")
    public RestResult getRoleMpList(@Validated @RequestBody GetRoleListMpValid req) {
        return roleMpService.getRoleMpList(req.getId(), req.getGid(), req.getSearch());
    }

    @PostMapping("/getUserRoleMp")
    public RestResult getUserRoleMp(@Validated @RequestBody GetUserRoleMpValid req) {
        return roleMpService.getUserRoleMp(req.getId(), req.getUid());
    }

    @PostMapping("/setUserRoleMp")
    public RestResult setUserRoleMp(@Validated @RequestBody SetUserRoleMpValid req) {
        return roleMpService.setUserRoleMp(req.getId(), req.getUid(), req.getRid());
    }

    @PostMapping("/getGroupRoleMp")
    public RestResult getGroupRoleMp(@Validated @RequestBody GetGroupRoleMpValid req) {
        return roleMpService.getGroupRoleMp(req.getId());
    }
}
