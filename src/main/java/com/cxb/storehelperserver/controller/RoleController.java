package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.role.*;
import com.cxb.storehelperserver.model.TRole;
import com.cxb.storehelperserver.service.RoleService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * desc: 角色接口
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@RestController
@RequestMapping("/api/role")
public class RoleController {
    @Resource
    private RoleService roleService;

    @PostMapping("/addRole")
    public RestResult addRole(@Validated @RequestBody AddRoleValid req) {
        TRole role = new TRole();
        role.setGid(req.getGid());
        role.setName(req.getName());
        role.setDescription(req.getDesc());
        return roleService.addRole(req.getId(), role, req.getPermissions());
    }

    @PostMapping("/setRole")
    public RestResult setRole(@Validated @RequestBody SetRoleValid req) {
        TRole role = new TRole();
        role.setId(req.getRid());
        role.setGid(req.getGid());
        role.setName(req.getName());
        role.setDescription(req.getDesc());
        return roleService.setRole(req.getId(), role, req.getPermissions());
    }

    @PostMapping("/delRole")
    public RestResult delRole(@Validated @RequestBody DelRoleValid req) {
        return roleService.delRole(req.getId(), req.getRid());
    }

    @PostMapping("/getRole")
    public RestResult getRole(@Validated @RequestBody GetRoleValid req) {
        return roleService.getRole(req.getId(), req.getRid());
    }

    @PostMapping("/getRoleList")
    public RestResult getRoleList(@Validated @RequestBody GetRoleListValid req) {
        return roleService.getRoleList(req.getId(), req.getGid(), req.getSearch());
    }

    @PostMapping("/getUserRole")
    public RestResult getUserRole(@Validated @RequestBody GetUserRoleValid req) {
        return roleService.getUserRole(req.getId(), req.getUid());
    }

    @PostMapping("/setUserRole")
    public RestResult setUserRole(@Validated @RequestBody SetUserRoleValid req) {
        return roleService.setUserRole(req.getId(), req.getUid(), req.getRid());
    }

    @PostMapping("/setUserRoleAdmin")
    public RestResult setUserRoleAdmin(@Validated @RequestBody SetUserRoleValid req) {
        return roleService.setUserRoleAdmin(req.getId(), req.getUid(), req.getRid());
    }

    @PostMapping("/getGroupRole")
    public RestResult getGroupRole(@Validated @RequestBody GetGroupRoleValid req) {
        return roleService.getGroupRole(req.getId());
    }
}
