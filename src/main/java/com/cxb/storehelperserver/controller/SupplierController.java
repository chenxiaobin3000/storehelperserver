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

    @PostMapping("/add")
    public RestResult add(@Validated @RequestBody AddSupplierValid req) {
        TSupplier supplier = new TSupplier();
        supplier.setName(req.getName());
        supplier.setPhone(req.getPhone());
        return supplierService.add(req.getId(), supplier);
    }

    @PostMapping("/set")
    public RestResult set(@Validated @RequestBody SetSupplierValid req) {
        TSupplier supplier = new TSupplier();
        supplier.setId(req.getSid());
        supplier.setName(req.getName());
        supplier.setPhone(req.getPhone());
        return supplierService.set(req.getId(), supplier);
    }

    @PostMapping("/del")
    public RestResult del(@Validated @RequestBody DelSupplierValid req) {
        return supplierService.del(req.getId(), req.getSid());
    }

    @PostMapping("/get")
    public RestResult get(@Validated @RequestBody GetSupplierValid req) {
        return supplierService.get(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getAll")
    public RestResult getAll(@Validated @RequestBody GetAllSupplierValid req) {
        return supplierService.getAll(req.getId());
    }
}
