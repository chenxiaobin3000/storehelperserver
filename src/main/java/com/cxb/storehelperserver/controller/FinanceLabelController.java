package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.financeLabel.AddLabelValid;
import com.cxb.storehelperserver.controller.request.financeLabel.DelLabelValid;
import com.cxb.storehelperserver.controller.request.financeLabel.GetGroupLabelValid;
import com.cxb.storehelperserver.controller.request.financeLabel.SetLabelValid;
import com.cxb.storehelperserver.model.TFinanceLabel;
import com.cxb.storehelperserver.service.FinanceLabelService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 财务类目接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/finance")
public class FinanceLabelController {
    @Resource
    private FinanceLabelService financeLabelService;

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
