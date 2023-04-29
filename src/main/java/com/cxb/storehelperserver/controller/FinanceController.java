package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.finance.*;
import com.cxb.storehelperserver.model.TFinanceLabel;
import com.cxb.storehelperserver.service.FinanceLabelService;
import com.cxb.storehelperserver.service.FinanceService;
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
import java.util.Date;

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

    @Resource
    private FinanceLabelService financeLabelService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/getFinance")
    public RestResult getFinance(@Validated @RequestBody GetFinanceValid req) {
        return financeService.getFinance(req.getId(), req.getPage(), req.getLimit(), req.getAction());
    }

    @PostMapping("/getLabelList")
    public RestResult getLabelList(@Validated @RequestBody GetLabelListValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return financeService.getLabelList(req.getId(), req.getPage(), req.getLimit(), req.getAction(), date);
    }

    @PostMapping("/insertLabelDetail")
    public RestResult insertLabelDetail(@Validated @RequestBody AddLabelDetailValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(req.getDate() + " 00:00:00");
        } catch (ParseException e) {
            return RestResult.fail("查询日期转换失败");
        }
        return financeService.insertLabelDetail(req.getId(), req.getGid(), req.getAction(), req.getAid(), req.getValue(), req.getRemark(), date, req.getSub());
    }

    @PostMapping("/addLabel")
    public RestResult addLabel(@Validated @RequestBody AddLabelValid req) {
        TFinanceLabel financeLabel = new TFinanceLabel();
        financeLabel.setGid(req.getGid());
        financeLabel.setName(req.getName());
        financeLabel.setParent(req.getParent());
        financeLabel.setLevel(req.getLevel());
        return financeLabelService.addabel(req.getId(), financeLabel);
    }

    @PostMapping("/setLabel")
    public RestResult setLabel(@Validated @RequestBody SetLabelValid req) {
        TFinanceLabel financeLabel = new TFinanceLabel();
        financeLabel.setId(req.getCid());
        financeLabel.setGid(req.getGid());
        financeLabel.setName(req.getName());
        financeLabel.setParent(req.getParent());
        financeLabel.setLevel(req.getLevel());
        return financeLabelService.setLabel(req.getId(), financeLabel);
    }

    @PostMapping("/delLabel")
    public RestResult delLabel(@Validated @RequestBody DelLabelValid req) {
        return financeLabelService.delLabel(req.getId(), req.getCid());
    }

    @PostMapping("/getGroupLabelList")
    public RestResult getGroupLabelList(@Validated @RequestBody GetGroupLabelValid req) {
        return financeLabelService.getGroupLabelList(req.getId());
    }

    @PostMapping("/getGroupLabelTree")
    public RestResult getGroupLabelTree(@Validated @RequestBody GetGroupLabelValid req) {
        return financeLabelService.getGroupLabelTree(req.getId());
    }
}
