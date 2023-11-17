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
    @PostMapping("/add")
    public RestResult add(@Validated @RequestBody AddUserValid req) {
        return userService.add(req.getId(), req.getAccount(), req.getPhone(), req.getDid(), req.getRid());
    }

    /**
     * desc: 设置用户信息
     */
    @PostMapping("/set")
    public RestResult set(@Validated @RequestBody SetUserValid req) {
        return userService.set(req.getId(), req.getUid(), req.getName(), req.getPhone());
    }

    /**
     * desc: 删除用户信息，仅将用户移除公司
     */
    @PostMapping("/del")
    public RestResult del(@Validated @RequestBody DelUserValid req) {
        return userService.del(req.getId(), req.getUid());
    }

    /**
     * desc: 获取用户信息
     */
    @PostMapping("/get")
    public RestResult get(@Validated @RequestBody GetUserValid req) {
        return userService.get(req.getId());
    }

    /**
     * desc: 获取用户信息
     */
    @PostMapping("/getByPhone")
    public RestResult getByPhone(@Validated @RequestBody GetUserByPhoneValid req) {
        return userService.getByPhone(req.getId(), req.getPhone());
    }

    /**
     * desc: 获取用户信息列表
     */
    @PostMapping("/getList")
    public RestResult getList(@Validated @RequestBody GetUserListValid req) {
        return userService.getList(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }
}
