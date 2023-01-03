package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
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
}
