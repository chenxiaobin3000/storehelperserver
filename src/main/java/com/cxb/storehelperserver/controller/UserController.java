package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.user.*;
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
     * desc: 添加用户信息
     */
    @PostMapping("/addUser")
    public RestResult addUser(@Validated @RequestBody AddUserValid req) {
        return userService.addUser(req.getId(), req.getAccount(), req.getPhone(), req.getRid());
    }

    /**
     * desc: 设置用户信息
     */
    @PostMapping("/setUser")
    public RestResult setUser(@Validated @RequestBody SetUserValid req) {
        return userService.setUser(req.getId(), req.getUid(), req.getName(), req.getPhone());
    }

    /**
     * desc: 删除用户信息，仅将用户移除公司
     */
    @PostMapping("/delUser")
    public RestResult delUser(@Validated @RequestBody DelUserValid req) {
        return userService.delUser(req.getId(), req.getUid());
    }

    /**
     * desc: 获取用户信息
     */
    @PostMapping("/getUser")
    public RestResult getUser(@Validated @RequestBody GetUserValid req) {
        return userService.getUser(req.getId());
    }

    /**
     * desc: 获取用户信息
     */
    @PostMapping("/getUserByPhone")
    public RestResult getUserByPhone(@Validated @RequestBody GetUserByPhoneValid req) {
        return userService.getUserByPhone(req.getId(), req.getPhone());
    }

    /**
     * desc: 获取用户信息列表
     */
    @PostMapping("/getUserList")
    public RestResult getUserList(@Validated @RequestBody GetUserListValid req) {
        return userService.getUserList(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
