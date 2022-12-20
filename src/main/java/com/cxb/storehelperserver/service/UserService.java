package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.repository.UserRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public RestResult getInfo(int id) {
        TUser user = userRepository.find(id);
        if (null == user) {
            return RestResult.fail("获取用户信息失败");
        }

        val roles = new ArrayList<String>();
        roles.add("dashboard_report");
        roles.add("agreement");
        roles.add("commodity");
        roles.add("finance");
        roles.add("market");
        roles.add("market_pdd");
        roles.add("market_meituan");
        roles.add("market_kuailv");
        roles.add("product");
        roles.add("report");
        roles.add("storage");
        roles.add("supplier");
        roles.add("user");
        roles.add("system");
        roles.add("admin");

        val data = new HashMap<String, Object>();
        data.put("name", user.getName());
        data.put("roles", roles);
        return RestResult.ok(data);
    }

    public RestResult getList() {
        return RestResult.ok();
    }
}
