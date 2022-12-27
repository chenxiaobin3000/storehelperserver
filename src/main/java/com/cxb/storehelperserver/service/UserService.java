package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.model.TUserRole;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.config.Permission.dashboard_userinfo;

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
    private UserGroupRepository userGroupRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    @Resource
    private RolePermissionRepository rolePermissionRepository;

    public RestResult getUserInfo(int id) {
        TUser user = userRepository.find(id);
        if (null == user) {
            return RestResult.fail("获取用户信息失败");
        }

        // 用户可以没有公司，不需要判断是否为空
        TUserGroup group = userGroupRepository.find(id);

        // 获取角色权限
        List<Integer> permissions = null;
        TUserRole userRole = userRoleRepository.find(id);
        if (null == userRole) {
            // 新客户默认只有首页
            permissions = new ArrayList<>();
            permissions.add(dashboard_userinfo);
        } else {
            permissions = rolePermissionRepository.find(userRole.getRid());
        }

        val data = new HashMap<String, Object>();
        data.put("user", user);
        data.put("group", group.getGid());
        data.put("permissions", permissions);
        return RestResult.ok(data);
    }

    public RestResult getUserList() {
        return RestResult.ok();
    }
}
