package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.config.GetPermissionValid;
import com.cxb.storehelperserver.service.ConfigService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * desc: 配置接口
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
public class ConfigController {
    @Resource
    private ConfigService configService;

    /**
     * desc: 获取用户信息
     */
    @PostMapping("/getPermission")
    public RestResult getInfo(@Validated @RequestBody GetPermissionValid req) {
        return configService.getPermission();
    }
}
