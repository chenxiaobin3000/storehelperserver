package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.service.model.PageData;
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

import static com.cxb.storehelperserver.util.Permission.*;

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
    private CheckService checkService;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private UserDepartmentRepository userDepartmentRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    @Resource
    private UserRoleMpRepository userRoleMpRepository;

    @Resource
    private RolePermissionRepository rolePermissionRepository;

    @Resource
    private RolePermissionMpRepository rolePermissionMpRepository;

    @Resource
    private GroupRepository groupRepository;

    @Resource
    private GroupMarketRepository groupMarketRepository;

    @Resource
    private DepartmentRepository departmentRepository;

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private RoleMpRepository roleMpRepository;

    @Resource
    private AccountRepository accountRepository;

    @Value("${store-app.config.defaultpwd}")
    private String defaultpwd;

    public RestResult addUser(int id, String account, String phone, int rid) {
        // 验证角色信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        if (null != accountRepository.find(account)) {
            return RestResult.fail("账号已存在");
        }

        List<TRole> roles = roleRepository.findByGroup(group.getGid());
        if (null == roles) {
            return RestResult.fail("获取角色信息失败");
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

        TUser user = userRepository.insert(account, phone);
        if (null == user) {
            return RestResult.fail("新增用户信息失败");
        }

        if (!userGroupRepository.insert(user.getId(), group.getGid())) {
            return RestResult.fail("新增用户公司信息失败");
        }

        if (!userRoleRepository.insert(user.getId(), rid)) {
            return RestResult.fail("新增用户角色信息失败");
        }

        if (!accountRepository.insert(account, defaultpwd, user.getId())) {
            return RestResult.fail("新增账号信息失败");
        }
        return RestResult.ok(user);
    }

    public RestResult setUser(int id, int uid, String name, String phone) {
        // 操作员必须同公司用户
        TUserGroup group = userGroupRepository.find(id);
        TUserGroup group2 = userGroupRepository.find(uid);
        if (null == group || null == group2 || !group2.getGid().equals(group.getGid())) {
            return RestResult.fail("操作仅限本公司");
        }

        // 权限校验
        if (!checkService.checkRolePermission(id, user_userlist)) {
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
        if (!checkService.checkRolePermission(id, user_userlist)) {
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

        // 获取小程序角色权限
        List<Integer> permissionMps = null;
        TUserRoleMp userRoleMp = userRoleMpRepository.find(id);
        if (null != userRoleMp) {
            permissionMps = rolePermissionMpRepository.find(userRoleMp.getRid());
        }

        // 获取公司信息，用户可以没有公司
        val data = new HashMap<String, Object>();
        TUserGroup userGroup = userGroupRepository.find(id);
        if (null == userGroup) {
            data.put("group", null);
        } else {
            TGroup group = groupRepository.find(userGroup.getGid());
            if (null == group) {
                return RestResult.fail("获取用户公司信息失败");
            }
            data.put("group", group);

            // 获取公司授权平台信息
            val market = new ArrayList<>();
            val markets = groupMarketRepository.find(group.getId());
            if (null != markets && !markets.isEmpty()) {
                for (TGroupMarket m : markets) {
                    market.add(m.getMid());
                }
            }
            data.put("market", market);
        }

        // 获取部门信息
        TUserDepartment userDepartment = userDepartmentRepository.find(id);
        if (null == userDepartment) {
            data.put("depart", null);
        } else {
            TDepartment department = departmentRepository.find(userDepartment.getDid());
            if (null == department) {
                return RestResult.fail("获取用户部门信息失败");
            }
            data.put("depart", department);
        }

        data.put("user", user);
        data.put("perms", permissions);
        data.put("permMps", permissionMps);
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
            return RestResult.ok(new PageData());
        }

        val list = userRepository.pagination(page, limit, group.getGid(), search);
        if (null == list) {
            return RestResult.fail("未查询到公司信息");
        }

        // 查询角色信息
        val list2 = new ArrayList<HashMap<String, Object>>();
        for (TUser u : list) {
            int uid = u.getId();
            val user = new HashMap<String, Object>();
            user.put("id", uid);
            user.put("name", u.getName());
            user.put("phone", u.getPhone());

            // 角色信息
            TUserRole userRole = userRoleRepository.find(uid);
            if (null != userRole) {
                user.put("role", roleRepository.find(userRole.getRid()));
            } else {
                user.put("role", null);
            }

            // 小程序角色信息
            TUserRoleMp userRoleMp = userRoleMpRepository.find(uid);
            if (null != userRoleMp) {
                user.put("rolemp", roleMpRepository.find(userRoleMp.getRid()));
            } else {
                user.put("rolemp", null);
            }

            // 获取部门信息
            TUserDepartment userDepartment = userDepartmentRepository.find(uid);
            if (null == userDepartment) {
                user.put("depart", null);
            } else {
                TDepartment department = departmentRepository.find(userDepartment.getDid());
                if (null == department) {
                    return RestResult.fail("获取用户部门信息失败");
                }
                user.put("depart", department);
            }
            list2.add(user);
        }
        return RestResult.ok(new PageData(total, list2));
    }
}
