package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.storage.*;
import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.model.TStorageOrder;
import com.cxb.storehelperserver.service.StorageService;
import com.cxb.storehelperserver.service.StorageStockService;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.cxb.storehelperserver.util.TypeDefine.OrderInOutType;

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
    private StorageStockService storageStockService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/addStorage")
    public RestResult addStorage(@Validated @RequestBody AddStorageValid req) {
        TStorage storage = new TStorage();
        storage.setGid(req.getGid());
        storage.setArea(Long.valueOf(req.getArea()));
        storage.setContact(req.getContact());
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
        return storageService.addStorage(req.getId(), storage);
    }

    @PostMapping("/setStorage")
    public RestResult setStorage(@Validated @RequestBody SetStorageValid req) {
        TStorage storage = new TStorage();
        storage.setId(req.getSid());
        storage.setGid(req.getGid());
        storage.setArea(Long.valueOf(req.getArea()));
        storage.setContact(req.getContact());
        storage.setName(req.getName());
        storage.setAddress(req.getAddress());
        return storageService.setStorage(req.getId(), storage);
    }

    @PostMapping("/delStorage")
    public RestResult delStorage(@Validated @RequestBody DelStorageValid req) {
        return storageService.delStorage(req.getId(), req.getGid(), req.getSid());
    }

    @PostMapping("/getGroupStorage")
    public RestResult getGroupStorage(@Validated @RequestBody GetGroupStorageValid req) {
        return storageService.getGroupStorage(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/purchase")
    public RestResult purchase(@Validated @RequestBody PurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
        TStorageOrder order = new TStorageOrder();
        order.setGid(req.getGid());
        order.setBatch(req.getBatch());
        order.setSid(req.getSid());
        order.setOtype(OrderInOutType.IN_ORDER.getValue());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换错误");
        }
        return storageStockService.purchase(req.getId(), order, req.getTypes(),
                req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
    }

    @PostMapping("/getStorageOrder")
    public RestResult getStorageOrder(@Validated @RequestBody GetStorageOrderValid req) {
        return storageStockService.getStorageOrder(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
