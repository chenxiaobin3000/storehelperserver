package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    private UserRepository userRepository;

    @Value("${store.user.uid}")
    public int preUid;

    /**
     * desc: 注册用户，用户 id:0 用于错误返回
     */
    public String register() {
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }

    public String login() {
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }

    public String logout() {
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }

    public String getUserInfo() {
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }

    public String getUserList() {
        TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        return user.getPhone();
    }
}
