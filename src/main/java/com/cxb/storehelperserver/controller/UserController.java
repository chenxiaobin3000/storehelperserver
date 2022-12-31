package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.user.GetUserInfoByPhoneValid;
import com.cxb.storehelperserver.controller.request.user.GetUserInfoValid;
import com.cxb.storehelperserver.controller.request.user.GetUserListValid;
import com.cxb.storehelperserver.service.UserService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * desc: 用户接口
 * auth: cxb
 * date: 2022/11/29
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * desc: 获取用户信息
     */
    @PostMapping("/getUserInfo")
    public RestResult getUserInfo(@Validated @RequestBody GetUserInfoValid req) {
        return userService.getUserInfo(req.getId());
    }

    /**
     * desc: 获取用户信息
     */
    @PostMapping("/getUserInfoByPhone")
    public RestResult getUserInfoByPhone(@Validated @RequestBody GetUserInfoByPhoneValid req) {
        return userService.getUserInfoByPhone(req.getId(), req.getPhone());
    }

    /**
     * desc: 获取用户信息列表
     */
    @PostMapping("/getUserList")
    public RestResult getUserList(@Validated @RequestBody GetUserListValid req) {
        return userService.getUserList(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
