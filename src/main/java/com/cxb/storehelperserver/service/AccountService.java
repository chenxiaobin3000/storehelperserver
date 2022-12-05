package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TAccount;
import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.repository.AccountRepository;
import com.cxb.storehelperserver.repository.UserRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * desc: 账号业务
 * auth: cxb
 * date: 2022/11/29
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountService {
    @Resource
    private SessionService sessionService;

    @Resource
    private AccountRepository accountRepository;

    @Resource
    private UserRepository userRepository;

    // 新用户默认公司id
    @Value("${store-app.config.group}")
    private int newUserGid;

    // TODO mybatis 多源

    /**
     * desc: 注册用户，用户 id:0 用于错误返回
     */
    public RestResult register(String account, String password, String phone) {
        // 验证账号是否存在
        TAccount tAccount = accountRepository.find(account);
        if (null != tAccount) {
            return RestResult.fail("账号已经存在");
        }

        // 生成用户
        TUser user = new TUser();
        user.setName(account);
        user.setPhone(phone);
        if (!userRepository.insert(user)) {
            return RestResult.fail("注册用户失败");
        }

        // 生成账号
        tAccount = new TAccount();
        tAccount.setAccount(account);
        tAccount.setPassword(password);
        tAccount.setUid(user.getId());
        tAccount.setGid(newUserGid);
        if (!accountRepository.insert(tAccount)) {
            return RestResult.fail("注册账号失败");
        }
        return RestResult.ok();
    }

    public RestResult login(String account, String password) {
        TAccount tAccount = accountRepository.find(account);
        if (null == tAccount) {
            RestResult.fail("账号不存在");
        }

        // 校验密码
        if (!tAccount.getPassword().equals(password)) {
            RestResult.fail("密码不正确");
        }

        // 生成 session
        String session = sessionService.create(tAccount);
        if (null == session) {
            RestResult.fail("登陆失败");
        }
        val data = new HashMap<String, Object>();
        data.put("token", session);
        return RestResult.ok(data);
    }

    public RestResult logout(int id) {
        if (!sessionService.delete(id)) {
            return RestResult.fail("登出失败");
        }
        return RestResult.ok();
    }

    public RestResult getAccountInfo() {
        return RestResult.ok();
    }

    public RestResult getAccountList() {
        return RestResult.ok();
    }
}
