package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.department.*;
import com.cxb.storehelperserver.model.TDepartment;
import com.cxb.storehelperserver.service.DepartmentService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 部门接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/department")
public class DepartmentController {
    @Resource
    private DepartmentService departmentService;

    @PostMapping("/addDepartment")
    public RestResult addDepartment(@Validated @RequestBody AddDepartmentValid req) {
        TDepartment department = new TDepartment();
        department.setGid(req.getGid());
        department.setName(req.getName());
        department.setParent(req.getParent());
        department.setLevel(req.getLevel());
        return departmentService.addDepartment(req.getId(), department);
    }

    @PostMapping("/setDepartment")
    public RestResult setDepartment(@Validated @RequestBody SetDepartmentValid req) {
        TDepartment department = new TDepartment();
        department.setId(req.getPid());
        department.setGid(req.getGid());
        department.setName(req.getName());
        department.setParent(req.getParent());
        department.setLevel(req.getLevel());
        return departmentService.setDepartment(req.getId(), department);
    }

    @PostMapping("/delDepartment")
    public RestResult delDepartment(@Validated @RequestBody DelDepartmentValid req) {
        return departmentService.delDepartment(req.getId(), req.getPid());
    }

    @PostMapping("/getGroupDepartmentList")
    public RestResult getGroupDepartmentList(@Validated @RequestBody GetGroupDepartmentValid req) {
        return departmentService.getGroupDepartmentList(req.getId());
    }

    @PostMapping("/getGroupDepartmentTree")
    public RestResult getGroupDepartmentTree(@Validated @RequestBody GetGroupDepartmentValid req) {
        return departmentService.getGroupDepartmentTree(req.getId());
    }

    @PostMapping("/setUserDepartment")
    public RestResult setUserDepartment(@Validated @RequestBody SetUserDepartmentValid req) {
        return departmentService.setUserDepartment(req.getId(), req.getUid(), req.getGid(), req.getDid());
    }
}
