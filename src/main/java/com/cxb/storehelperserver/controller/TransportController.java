package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.transport.*;
import com.cxb.storehelperserver.service.TransportService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.cxb.storehelperserver.util.TypeDefine.BusinessType;

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

    @PostMapping("/addOrderFare")
    public RestResult addOrderFare(@Validated @RequestBody AddOrderFareValid req) {
        return transportService.addOrderFare(req.getId(), BusinessType.valueOf(req.getOtype()), req.getOid(), req.getShip(),
                req.getCode(), req.getPhone(), req.getFare(), req.getRemark());
    }

    @PostMapping("/delOrderFare")
    public RestResult delOrderFare(@Validated @RequestBody DelOrderFareValid req) {
        return transportService.delOrderFare(req.getId(), BusinessType.valueOf(req.getOtype()), req.getOid(), req.getFid());
    }
}
