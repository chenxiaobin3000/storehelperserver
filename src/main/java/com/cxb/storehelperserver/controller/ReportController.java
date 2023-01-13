package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.report.*;
import com.cxb.storehelperserver.service.ReportService;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.TypeDefine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 报表接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
public class ReportController {
    @Resource
    private ReportService reportService;

    @PostMapping("/getAgreement")
    public RestResult getAgreement(@Validated @RequestBody AgreementValid req) {
        return reportService.getAgreement(req.getId(), req.getGid(), TypeDefine.ReportCycleType.valueOf(req.getType()));
    }

    @PostMapping("/getFinance")
    public RestResult getFinance(@Validated @RequestBody FinanceValid req) {
        return reportService.getFinance(req.getId(), req.getGid(), TypeDefine.ReportCycleType.valueOf(req.getType()));
    }

    @PostMapping("/getMarket")
    public RestResult getMarket(@Validated @RequestBody MarketValid req) {
        return reportService.getMarket(req.getId(), req.getGid(), TypeDefine.ReportCycleType.valueOf(req.getType()));
    }

    @PostMapping("/getProduct")
    public RestResult getProduct(@Validated @RequestBody ProductValid req) {
        return reportService.getProduct(req.getId(), req.getGid(), TypeDefine.ReportCycleType.valueOf(req.getType()));
    }

    @PostMapping("/getStorage")
    public RestResult getStorage(@Validated @RequestBody StorageValid req) {
        return reportService.getStorage(req.getId(), req.getGid(), TypeDefine.ReportCycleType.valueOf(req.getType()));
    }
}
