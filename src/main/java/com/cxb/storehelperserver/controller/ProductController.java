package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.service.ProductService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 生产接口
 * auth: cxb
 * date: 2023/1/11
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Resource
    private ProductService productService;

    @PostMapping("/addProduct")
    public RestResult addProduct(@Validated @RequestBody AddProductValid req) {
        TStorage storage = new TStorage();
        storage.setGid(req.getGid());
        storage.setArea(Long.valueOf(req.getArea()));
        storage.setContact(req.getContact());
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
        return storageService.addStorage(req.getId(), storage);
    }

    @PostMapping("/setProduct")
    public RestResult setProduct(@Validated @RequestBody SetProductValid req) {
        TStorage storage = new TStorage();
        storage.setId(req.getSid());
        storage.setGid(req.getGid());
        storage.setArea(Long.valueOf(req.getArea()));
        storage.setContact(req.getContact());
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
        return storageService.setStorage(req.getId(), storage);
    }

    @PostMapping("/delProduct")
    public RestResult delProduct(@Validated @RequestBody DelProductValid req) {
        return storageService.delStorage(req.getId(), req.getGid(), req.getSid());
    }

    @PostMapping("/getGroupProduct")
    public RestResult getGroupProduct(@Validated @RequestBody GetGroupProductValid req) {
        return storageService.getGroupStorage(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
