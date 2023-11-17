package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.PageData;
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
    private RolePermissionRepository rolePermissionRepository;

    @Resource
    private DepartmentRepository departmentRepository;

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private AccountRepository accountRepository;

    @Value("${store-app.config.defaultpwd}")
    private String defaultpwd;

    public RestResult add(int id, String account, String phone, int did, int rid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_userlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        if (null != accountRepository.find(account)) {
            return RestResult.fail("账号已存在");
        }

        // 部门
        List<TDepartment> departments = departmentRepository.all();
        if (null == departments) {
            return RestResult.fail("获取部门信息失败");
        }
        boolean find = false;
        for (TDepartment department : departments) {
            if (department.getId().equals(did)) {
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("未查询到部门信息");
        }

        // 角色
        List<TRole> roles = roleRepository.all(null);
        if (null == roles) {
            return RestResult.fail("获取角色信息失败");
        }
        find = false;
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
        if (!accountRepository.insert(account, defaultpwd, user.getId())) {
            return RestResult.fail("新增账号信息失败");
        }
        return RestResult.ok(user);
    }

    public RestResult set(int id, int uid, String name, String phone) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_userlist)) {
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

    public RestResult del(int id, int uid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_userlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        if (!userRepository.delete(uid)) {
            return RestResult.fail("删除用户信息失败");
        }
        return RestResult.ok();
    }

    public RestResult get(int id) {
        TUser user = userRepository.find(id);
        if (null == user) {
            return RestResult.fail("获取用户信息失败");
        }

        // 获取公司信息，用户可以没有公司
        val data = new HashMap<String, Object>();
        data.put("user", user);

        // 获取部门信息
        TDepartment department = departmentRepository.find(user.getDid());
        if (null == department) {
            return RestResult.fail("获取用户部门信息失败");
        }
        data.put("depart", department);

        // 获取公司授权平台信息
//        val market = new ArrayList<>();
//        val markets = groupMarketRepository.find(group.getId());
//        if (null != markets && !markets.isEmpty()) {
//            for (TGroupMarket m : markets) {
//                market.add(m.getMid());
//            }
//        }
//        data.put("market", market);

        // 获取角色权限
        data.put("perms", rolePermissionRepository.find(user.getRid()));
        return RestResult.ok(data);
    }

    public RestResult getByPhone(int id, String phone) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_userlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }
        return RestResult.ok(userRepository.findByPhone(phone));
    }

    public RestResult getList(int id, int page, int limit, String search) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_userlist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        int total = userRepository.total(search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = userRepository.pagination(page, limit, search);
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
            user.put("role", roleRepository.find(u.getRid()));

            // 获取部门信息
            TDepartment department = departmentRepository.find(u.getDid());
            if (null == department) {
                return RestResult.fail("获取用户部门信息失败");
            }
            user.put("depart", department);
            list2.add(user);
        }
        return RestResult.ok(new PageData(total, list2));
    }
}
