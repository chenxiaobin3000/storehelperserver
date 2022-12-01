package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.user.LoginValid;
import com.cxb.storehelperserver.controller.request.user.RegisterValid;
import com.cxb.storehelperserver.service.UserService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * desc: 用户接口
 * auth: cxb
 * date: 2022/11/29
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * desc: 注册
     */
    @PostMapping("/register")
    public RestResult register(@RequestBody RegisterValid req) {
        return userService.register(req.getAccount(), req.getPassword());
    }

    /**
     * desc: 登录
     */
    @PostMapping("/login")
    public RestResult login(@Validated @RequestBody LoginValid req) {
        val ret = new HashMap<String, Object>();
        ret.put("account", req.getAccount());
        ret.put("sql", userService.login());
        return RestResult.ok(ret);
    }

    /**
     * desc: 登出
     */
    @PostMapping("/logout")
    public RestResult logout(@RequestBody Map req) {
        String account = (String) req.get("account");
        val ret = new HashMap<String, Object>();
        ret.put("account", account);
        ret.put("sql", userService.login());
        return RestResult.ok(ret);
    }

    /**
     * desc: 获取用户信息
     */
    @PostMapping("/getUserInfo")
    public RestResult getUserInfo(@RequestBody Map req) {
        String account = (String) req.get("account");
        val ret = new HashMap<String, Object>();
        ret.put("account", account);
        ret.put("sql", userService.login());
        return RestResult.ok(ret);
    }

    /**
     * desc: 获取用户信息列表
     */
    @PostMapping("/getUserList")
    public RestResult getUserList(@RequestBody Map req) {
        String account = (String) req.get("account");
        val ret = new HashMap<String, Object>();
        ret.put("account", account);
        ret.put("sql", userService.login());
        return RestResult.ok(ret);
    }
}
