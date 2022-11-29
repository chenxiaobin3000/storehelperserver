package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * desc: 用户业务
 * auth: cxb
 * date: 2022/11/29
 */
@Service
@Transactional
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Resource
    private UserRepository userRepository;

    /**
     * desc:
     */
    public String getUserName() {
        logger.info("get user name");
        return userRepository.getUserName();
    }
}
