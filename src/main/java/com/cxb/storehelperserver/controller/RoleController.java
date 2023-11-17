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

    @PostMapping("/add")
    public RestResult add(@Validated @RequestBody AddRoleValid req) {
        TRole role = new TRole();
        role.setName(req.getName());
        return roleService.add(req.getId(), role, req.getPermissions());
    }

    @PostMapping("/set")
    public RestResult set(@Validated @RequestBody SetRoleValid req) {
        TRole role = new TRole();
        role.setId(req.getRid());
        role.setName(req.getName());
        return roleService.set(req.getId(), role, req.getPermissions());
    }

    @PostMapping("/del")
    public RestResult del(@Validated @RequestBody DelRoleValid req) {
        return roleService.del(req.getId(), req.getRid());
    }

    @PostMapping("/get")
    public RestResult get(@Validated @RequestBody GetRoleValid req) {
        return roleService.get(req.getId(), req.getRid());
    }

    @PostMapping("/getList")
    public RestResult getList(@Validated @RequestBody GetRoleListValid req) {
        return roleService.getList(req.getId(), req.getSearch());
    }

    @PostMapping("/getUserRole")
    public RestResult getUserRole(@Validated @RequestBody GetUserRoleValid req) {
        return roleService.getUserRole(req.getId(), req.getUid());
    }

    @PostMapping("/setUserRole")
    public RestResult setUserRole(@Validated @RequestBody SetUserRoleValid req) {
        return roleService.setUserRole(req.getId(), req.getUid(), req.getRid());
    }
}
