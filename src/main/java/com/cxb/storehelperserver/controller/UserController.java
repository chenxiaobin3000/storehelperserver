package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {
    //logger

    @Resource
    private UserService userService;

    /**
     *
     * @param req
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map req) {
        String account = (String)req.get("account");
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("account", account);
        ret.put("sql", userService.getUserName());
        return ResultResponse.ok(ret);
    }
}
