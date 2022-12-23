package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.role.AddRoleValid;
import com.cxb.storehelperserver.controller.request.role.DelRoleValid;
import com.cxb.storehelperserver.controller.request.role.SetRoleValid;
import com.cxb.storehelperserver.controller.request.role.GetUserRoleValid;
import com.cxb.storehelperserver.controller.request.role.SetUserRoleValid;
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
        return roleService.addRole(role);
    }

    @PostMapping("/setRole")
    public RestResult setRole(@Validated @RequestBody SetRoleValid req) {
        TRole role = new TRole();
        role.setGid(req.getGid());
        role.setName(req.getName());
        return roleService.setRole(role);
    }

    @PostMapping("/delRole")
    public RestResult delRole(@Validated @RequestBody DelRoleValid req) {
        return roleService.delRole(req.getId());
    }

    @PostMapping("/getUserRole")
    public RestResult getUserRole(@Validated @RequestBody GetUserRoleValid req) {
        return roleService.getUserRole(1);
    }

    @PostMapping("/setUserRole")
    public RestResult setUserRole(@Validated @RequestBody SetUserRoleValid req) {
        return roleService.setUserRole(1, 1);
    }
}
