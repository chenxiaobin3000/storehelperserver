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

import static com.cxb.storehelperserver.util.Permission.*;

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
    private CheckService checkService;

    @Resource
    private AccountRepository accountRepository;

    @Resource
    private UserRepository userRepository;

    @Value("${store-app.config.openreg}")
    private boolean openreg;

    @Value("${store-app.config.defaultpwd}")
    private String defaultpwd;

    /**
     * desc: 注册用户
     */
    public RestResult register(String account, String password, String phone) {
        if (!openreg) {
            return RestResult.fail("系统未开放注册功能，请联系管理员");
        }

        // 验证账号是否存在
        TAccount tAccount = accountRepository.find(account);
        if (null != tAccount) {
            return RestResult.fail("账号已经存在");
        }

        // 生成用户
        TUser user = userRepository.insert(account, phone);
        if (null == user) {
            return RestResult.fail("注册用户失败");
        }

        // 生成账号
        if (!accountRepository.insert(account, password, user.getId())) {
            return RestResult.fail("注册账号失败");
        }
        return RestResult.ok();
    }

    public RestResult login(String account, String password) {
        TAccount tAccount = accountRepository.find(account);
        if (null == tAccount) {
            return RestResult.fail("账号不存在");
        }

        // 校验密码
        if (!tAccount.getPassword().equals(password)) {
            return RestResult.fail("密码不正确");
        }

        // 生成 session
        String session = sessionService.create(tAccount);
        if (null == session) {
            return RestResult.fail("登陆失败");
        }
        val data = new HashMap<String, Object>();
        data.put("id", tAccount.getUid());
        data.put("token", session);
        return RestResult.ok(data);
    }

    public RestResult logout(int id) {
        if (!sessionService.delete(id)) {
            return RestResult.fail("登出失败");
        }
        return RestResult.ok();
    }

    public RestResult setPassword(int id, String oldPassword, String newPassword) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_setpassword)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }
        TAccount account = accountRepository.find(id);
        if (null == account) {
            return RestResult.fail("获取账号信息失败");
        }
        if (!account.getPassword().equals(oldPassword)) {
            return RestResult.fail("原始密码错误");
        }
        if (!accountRepository.update(id, newPassword)) {
            return RestResult.fail("重置密码失败");
        }
        return RestResult.ok();
    }

    public RestResult resetPwd(int id, int uid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_resetpwd)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }
        if (!accountRepository.update(uid, defaultpwd)) {
            return RestResult.fail("重置密码失败");
        }
        return RestResult.ok();
    }
}
