package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.storage.*;
import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.service.StorageService;
import com.cxb.storehelperserver.util.DateUtil;
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

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/add")
    public RestResult add(@Validated @RequestBody AddStorageValid req) {
        TStorage storage = new TStorage();
        storage.setArea(Long.valueOf(req.getArea()));
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
        return storageService.add(req.getId(), storage);
    }

    @PostMapping("/set")
    public RestResult set(@Validated @RequestBody SetStorageValid req) {
        TStorage storage = new TStorage();
        storage.setId(req.getSid());
        storage.setArea(Long.valueOf(req.getArea()));
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
        return storageService.set(req.getId(), storage);
    }

    @PostMapping("/del")
    public RestResult del(@Validated @RequestBody DelStorageValid req) {
        return storageService.del(req.getId(), req.getSid());
    }

    @PostMapping("/get")
    public RestResult get(@Validated @RequestBody GetGroupStorageValid req) {
        return storageService.get(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getAll")
    public RestResult getAll(@Validated @RequestBody GetGroupAllStorageValid req) {
        return storageService.getAll(req.getId());
    }
}
