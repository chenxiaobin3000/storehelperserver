package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * desc: 用户接口
 * auth: cxb
 * date: 2022/11/29
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Resource
    private UserService userService;

    /**
     * desc: 登录
     */
    @PostMapping("/login")
    public RestResult login(@RequestBody Map req) {
        String account = (String)req.get("account");
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("account", account);
        ret.put("sql", userService.getUserName());
        return RestResult.ok(ret);
    }
}
