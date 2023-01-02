package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.config.Permission.*;

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
    private RoleService roleService;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    @Resource
    private RolePermissionRepository rolePermissionRepository;

    @Resource
    private GroupRepository groupRepository;

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private AccountRepository accountRepository;

    @Value("${store-app.config.defaultpwd}")
    private String defaultpwd;

    public RestResult addUser(int id, String account, String phone, int rid) {
        // 验证角色信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        List<TRole> roles = roleRepository.findByGroup(group.getGid());
        if (null == roles) {
            return RestResult.fail("获取角色信息异常");
        }
        boolean find = false;
        for (TRole role : roles) {
            if (role.getId().equals(rid)) {
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("未查询到角色信息");
        }

        TUser user = new TUser();
        user.setName(account);
        user.setPhone(phone);
        if (!userRepository.insert(user)) {
            return RestResult.fail("新增用户信息失败");
        }

        TUserGroup userGroup = new TUserGroup();
        userGroup.setUid(user.getId());
        userGroup.setGid(group.getGid());
        if (!userGroupRepository.insert(userGroup)) {
            return RestResult.fail("新增用户公司信息失败");
        }

        TUserRole userRole = new TUserRole();
        userRole.setUid(user.getId());
        userRole.setRid(rid);
        if (!userRoleRepository.insert(userRole)) {
            return RestResult.fail("新增用户角色信息失败");
        }

        TAccount tAccount = new TAccount();
        tAccount.setAccount(account);
        tAccount.setPassword(defaultpwd);
        tAccount.setUid(user.getId());
        if (!accountRepository.insert(tAccount)) {
            return RestResult.fail("新增账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setUser(int id, int uid, String name, String phone) {
        // 操作员必须同公司用户
        TUserGroup group = userGroupRepository.find(id);
        TUserGroup group2 = userGroupRepository.find(uid);
        if (null == group || null == group2 || !group2.getGid().equals(group.getGid())) {
            return RestResult.fail("操作仅限本公司");
        }

        // 权限校验
        if (!roleService.checkRolePermission(id, user_getlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        TUser user = new TUser();
        user.setId(uid);
        user.setName(name);
        user.setPhone(phone);
        if (!userRepository.update(user)) {
            return RestResult.fail("修改用户信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delUser(int id, int uid) {
        // 操作员必须同公司用户
        TUserGroup group = userGroupRepository.find(id);
        TUserGroup group2 = userGroupRepository.find(uid);
        if (null == group || null == group2 || !group2.getGid().equals(group.getGid())) {
            return RestResult.fail("操作仅限本公司");
        }

        // 权限校验
        if (!roleService.checkRolePermission(id, user_getlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        if (!userGroupRepository.delete(uid)) {
            return RestResult.fail("删除用户信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getUser(int id) {
        TUser user = userRepository.find(id);
        if (null == user) {
            return RestResult.fail("获取用户信息失败");
        }

        // 用户可以没有公司，不需要判断是否为空
        TUserGroup userGroup = userGroupRepository.find(id);

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
        if (null == userGroup) {
            data.put("group", null);
        } else {
            TGroup group = groupRepository.find(userGroup.getGid());
            if (null == group) {
                return RestResult.fail("获取用户公司信息失败");
            }
            data.put("group", group);
        }

        data.put("user", user);
        data.put("permissions", permissions);
        return RestResult.ok(data);
    }

    public RestResult getUserByPhone(int id, String phone) {
        // 操作员必须同公司用户
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        TUser user = userRepository.findByPhone(phone);
        if (null == user) {
            return RestResult.fail("获取用户信息失败");
        }
        TUserGroup group2 = userGroupRepository.find(user.getId());
        if (null == group2) {
            return RestResult.fail("获取用户公司信息失败");
        }
        if (!group2.getGid().equals(group.getGid())) {
            return RestResult.fail("操作仅限本公司");
        }
        return RestResult.ok(user);
    }

    public RestResult getUserList(int id, int page, int limit, String search) {
        // 操作员必须同公司用户
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = userRepository.total(group.getGid(), search);
        if (0 == total) {
            return RestResult.fail("没有查询到任何公司信息");
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);

        // 查询角色信息
        val list = userRepository.pagination(page, limit, group.getGid(), search);
        if (null != list && !list.isEmpty()) {
            val list2 = new ArrayList<>();
            for (TUser u : list) {
                val user = new HashMap<String, Object>();
                user.put("id", u.getId());
                user.put("name", u.getName());
                user.put("phone", u.getPhone());
                TUserRole userRole = userRoleRepository.find(u.getId());
                if (null != userRole) {
                    user.put("role", roleRepository.find(userRole.getRid()));
                } else {
                    user.put("role", null);
                }
                user.put("part", null);
                list2.add(user);
            }
            data.put("list", list2);
        } else {
            data.put("list", null);
        }
        return RestResult.ok(data);
    }
}
