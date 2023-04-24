package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.product.CollectValid;
import com.cxb.storehelperserver.service.TransportService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc:
 * auth: cxb
 * date: 2023/4/24
 */
@Slf4j
@RestController
@RequestMapping("/api/transport")
public class TransportController {
    @Resource
    private TransportService transportService;

    @PostMapping("/getPurchaseFareList")
    public RestResult getPurchaseFareList(@Validated @RequestBody CollectValid req) {
        //return transportService.getPurchaseFareList();
        return null;
    }

    @PostMapping("/getStorageFareList")
    public RestResult getStorageFareList(@Validated @RequestBody CollectValid req) {
        return transportService.getStorageFareList();
    }

    @PostMapping("/getAgreementFareList")
    public RestResult getAgreementFareList(@Validated @RequestBody CollectValid req) {
        return transportService.getAgreementFareList();
    }
}
