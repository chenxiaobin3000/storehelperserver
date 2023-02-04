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

    @PostMapping("/getYesterday")
    public RestResult getToday(@Validated @RequestBody getTodayReportValid req) {
        return reportService.getTodayReport(req.getId(), req.getGid());
    }
}
