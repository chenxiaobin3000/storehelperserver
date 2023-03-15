package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.report.*;
import com.cxb.storehelperserver.service.ReportService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.cxb.storehelperserver.util.TypeDefine.ReportCycleType;

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

    @PostMapping("/getTodayReport")
    public RestResult getTodayReport(@Validated @RequestBody getTodayReportValid req) {
        return reportService.getTodayReport(req.getId(), req.getGid());
    }

    @PostMapping("/getMarketReport")
    public RestResult getMarketReport(@Validated @RequestBody getMarketReportValid req) {
        return reportService.getMarketReport(req.getId(), req.getGid(), req.getMid(), req.getCtype(), ReportCycleType.valueOf(req.getCycle()));
    }

    @PostMapping("/getAgreementReport")
    public RestResult getAgreementReport(@Validated @RequestBody getAgreementReportValid req) {
        return reportService.getAgreementReport(req.getId(), req.getGid(), req.getSid(), ReportCycleType.valueOf(req.getCycle()));
    }

    @PostMapping("/getCloudReport")
    public RestResult getCloudReport(@Validated @RequestBody getCloudReportValid req) {
        return reportService.getCloudReport(req.getId(), req.getGid(), req.getSid(), ReportCycleType.valueOf(req.getCycle()));
    }

    @PostMapping("/getProductReport")
    public RestResult getProductReport(@Validated @RequestBody getProductReportValid req) {
        return reportService.getProductReport(req.getId(), req.getGid(), req.getSid(), ReportCycleType.valueOf(req.getCycle()));
    }

    @PostMapping("/getPurchaseReport")
    public RestResult getPurchaseReport(@Validated @RequestBody getPurchaseReportValid req) {
        return reportService.getPurchaseReport(req.getId(), req.getGid(), req.getSid(), ReportCycleType.valueOf(req.getCycle()));
    }

    @PostMapping("/getStorageReport")
    public RestResult getStorageReport(@Validated @RequestBody getStorageReportValid req) {
        return reportService.getStorageReport(req.getId(), req.getGid(), req.getSid(), ReportCycleType.valueOf(req.getCycle()));
    }
}
