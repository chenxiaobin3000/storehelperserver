package com.cxb.storehelperserver.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * desc: 启动初始化
 * auth: cxb
 * date: 2022/12/1
 */
@Slf4j
@Component
public class StarterHandle {

    @PostConstruct
    public void init() throws InterruptedException {

    }
}
