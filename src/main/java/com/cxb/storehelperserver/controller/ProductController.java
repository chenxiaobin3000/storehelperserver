package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.product.CompleteValid;
import com.cxb.storehelperserver.controller.request.product.ProcessValid;
import com.cxb.storehelperserver.service.ProductService;
import com.cxb.storehelperserver.service.StorageStockService;
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

    @Resource
    private StorageStockService storageStockService;

    @PostMapping("/process")
    public RestResult process(@Validated @RequestBody ProcessValid req) {
        return storageStockService.process(req.getId(), req.getGid(), req.getSid(), req.getCommoditys(), req.getValues(), req.getPrices());
    }

    @PostMapping("/complete")
    public RestResult complete(@Validated @RequestBody CompleteValid req) {
        return storageStockService.complete(req.getId(), req.getGid(), req.getSid(), req.getCommoditys(), req.getValues(), req.getPrices());
    }
}
