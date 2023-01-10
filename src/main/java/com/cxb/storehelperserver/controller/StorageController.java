package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.storage.*;
import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.service.StorageService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 仓库接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/storage")
public class StorageController {
    @Resource
    private StorageService storageService;

    @PostMapping("/addStorage")
    public RestResult addStorage(@Validated @RequestBody AddStorageValid req) {
        TStorage storage = new TStorage();
        storage.setArea(req.getArea());
        storage.setContact(req.getContact());
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
        return storageService.addStorage(storage);
    }

    @PostMapping("/setStorage")
    public RestResult setStorage(@Validated @RequestBody SetStorageValid req) {
        TStorage storage = new TStorage();
        storage.setId(req.getGid());
        storage.setArea(req.getArea());
        storage.setContact(req.getContact());
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
        return storageService.setStorage(storage);
    }

    @PostMapping("/delStorage")
    public RestResult delStorage(@Validated @RequestBody DelStorageValid req) {
        return storageService.delStorage(req.getId(), req.getGid());
    }

    @PostMapping("/getGroupStorage")
    public RestResult getStorageList(@Validated @RequestBody GetStorageListValid req) {
        return storageService.getGroupStorage(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
