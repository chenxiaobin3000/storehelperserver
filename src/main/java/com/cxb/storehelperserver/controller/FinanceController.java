package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.finance.GetFinanceValid;
import com.cxb.storehelperserver.service.FinanceService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 财务接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/finance")
public class FinanceController {
    @Resource
    private FinanceService financeService;

    @PostMapping("/getFinance")
    public RestResult getFinance(@Validated @RequestBody GetFinanceValid req) {
        return financeService.getFinance(req.getId(), req.getPage(), req.getLimit(), req.getAction());
    }
}
