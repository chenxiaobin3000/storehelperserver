package com.cxb.storehelperserver.handle;

import com.cxb.storehelperserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * desc: 启动初始化
 * auth: cxb
 * date: 2022/12/1
 */
@Slf4j
@Component
public class StarterHandle {
    @Resource
    private UserService userService;

    /**
     * desc: 获取用户最大id
     */
    @PostConstruct
    public void init() throws InterruptedException {
        userService.init();
    }
}
