package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TAccount;
import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.repository.AccountRepository;
import com.cxb.storehelperserver.repository.UserRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

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

    @Resource
    private AccountRepository accountRepository;

    @Value("${store.user.uid}")
    private int preUid;

    private int curUid;

    /**
     * desc: 初始化用户 id
     */
    public void init() {
        int max = userRepository.findMaxUid();
        curUid = preUid + max;
    }

    /**
     * desc: 注册用户，用户 id:0 用于错误返回
     */
    public RestResult register(String account, String password) {
        // 验证账号是否存在
        TAccount tAccount = accountRepository.find(account);
        if (null != tAccount) {
            return RestResult.fail(-1, "账号已经存在");
        }

        // 生成用户
        TUser user = new TUser();

        // 生成账号
        //TUser user = userRepository.find(1);
        user.setName("test2");
        userRepository.update(user);
        val ret = new HashMap<String, Object>();
        return RestResult.ok();
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
