package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * desc: 用户业务
 * auth: cxb
 * date: 2022/11/29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Resource
    private UserRepository userRepository;

    @Value("${store.user.uid}")
    public int preUid;

    public String register() {
        logger.info("get pre uid:" + preUid);
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }

    public String login() {
        logger.info("get pre uid:" + preUid);
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }

    public String logout() {
        logger.info("get pre uid:" + preUid);
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }

    public String getUserInfo() {
        logger.info("get pre uid:" + preUid);
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }

    public String getUserList() {
        logger.info("get pre uid:" + preUid);
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }
}
