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

    @PostMapping("/add")
    public RestResult add(@Validated @RequestBody AddDepartmentValid req) {
        TDepartment department = new TDepartment();
        department.setName(req.getName());
        department.setParent(req.getParent());
        department.setLevel(req.getLevel());
        return departmentService.add(req.getId(), department);
    }

    @PostMapping("/set")
    public RestResult set(@Validated @RequestBody SetDepartmentValid req) {
        TDepartment department = new TDepartment();
        department.setId(req.getPid());
        department.setName(req.getName());
        department.setParent(req.getParent());
        department.setLevel(req.getLevel());
        return departmentService.set(req.getId(), department);
    }

    @PostMapping("/del")
    public RestResult del(@Validated @RequestBody DelDepartmentValid req) {
        return departmentService.del(req.getId(), req.getPid());
    }

    @PostMapping("/getList")
    public RestResult getList(@Validated @RequestBody GetDepartmentValid req) {
        return departmentService.getList(req.getId());
    }

    @PostMapping("/getTree")
    public RestResult getTree(@Validated @RequestBody GetDepartmentValid req) {
        return departmentService.getTree(req.getId());
    }

    @PostMapping("/setUserDepartment")
    public RestResult setUserDepartment(@Validated @RequestBody SetUserDepartmentValid req) {
        return departmentService.setUserDepartment(req.getId(), req.getUid(), req.getDid());
    }
}
