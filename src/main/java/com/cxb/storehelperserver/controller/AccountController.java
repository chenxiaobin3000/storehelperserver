package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.account.*;
import com.cxb.storehelperserver.service.AccountService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 账号接口
 * auth: cxb
 * date: 2022/11/29
 */
@Slf4j
@RestController
@RequestMapping("/api/account")
public class AccountController {
    @Resource
    private AccountService accountService;

    /**
     * desc: 注册
     */
    @PostMapping("/register")
    public RestResult register(@Validated @RequestBody RegisterValid req) {
        return accountService.register(req.getAccount(), req.getPassword(), req.getPhone());
    }

    /**
     * desc: 登陆
     */
    @PostMapping("/login")
    public RestResult login(@Validated @RequestBody LoginValid req) {
        return accountService.login(req.getAccount(), req.getPassword());
    }

    /**
     * desc: 登出
     */
    @PostMapping("/logout")
    public RestResult logout(@Validated @RequestBody LogoutValid req) {
        return accountService.logout(req.getId());
    }
}
