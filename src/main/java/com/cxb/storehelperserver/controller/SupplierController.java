package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.supplier.*;
import com.cxb.storehelperserver.model.TSupplier;
import com.cxb.storehelperserver.service.SupplierService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 供应商接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/supplier")
public class SupplierController {
    @Resource
    private SupplierService supplierService;

    @PostMapping("/addSupplier")
    public RestResult addSupplier(@Validated @RequestBody AddSupplierValid req) {
        TSupplier supplier = new TSupplier();
        supplier.setGid(req.getGid());
        supplier.setName(req.getName());
        supplier.setContact(req.getContact());
        supplier.setPhone(req.getPhone());
        supplier.setRemark(req.getRemark());
        return supplierService.addSupplier(req.getId(), supplier);
    }

    @PostMapping("/setSupplier")
    public RestResult setSupplier(@Validated @RequestBody SetSupplierValid req) {
        TSupplier supplier = new TSupplier();
        supplier.setId(req.getSid());
        supplier.setGid(req.getGid());
        supplier.setName(req.getName());
        supplier.setContact(req.getContact());
        supplier.setPhone(req.getPhone());
        supplier.setRemark(req.getRemark());
        return supplierService.setSupplier(req.getId(), supplier);
    }

    @PostMapping("/delSupplier")
    public RestResult delSupplier(@Validated @RequestBody DelSupplierValid req) {
        return supplierService.delSupplier(req.getId(), req.getGid(), req.getSid());
    }

    @PostMapping("/getGroupSupplier")
    public RestResult getGroupSupplier(@Validated @RequestBody GetGroupSupplierValid req) {
        return supplierService.getGroupSupplier(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getGroupAllSupplier")
    public RestResult getGroupAllSupplier(@Validated @RequestBody GetGroupAllSupplierValid req) {
        return supplierService.getGroupAllSupplier(req.getId());
    }
}
