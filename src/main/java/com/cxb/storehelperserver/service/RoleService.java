package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.util.Permission.*;

/**
 * desc: 角色业务
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleService {
    @Resource
    private CheckService checkService;

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private RolePermissionRepository rolePermissionRepository;

    @Resource
    private UserRepository userRepository;

    public RestResult add(int id, TRole role, List<Integer> permissions) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_rolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 角色名重名检测
        if (roleRepository.check(role.getName(), 0)) {
            return RestResult.fail("角色名称已存在");
        }

        if (!roleRepository.insert(role)) {
            return RestResult.fail("添加角色信息失败");
        }

        if (!rolePermissionRepository.insert(role.getId(), permissions)) {
            return RestResult.fail("添加角色权限失败");
        }
        return RestResult.ok();
    }

    public RestResult set(int id, TRole role, List<Integer> permissions) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_rolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 角色名重名检测
        if (roleRepository.check(role.getName(), role.getId())) {
            return RestResult.fail("角色名称已存在");
        }

        if (!roleRepository.update(role)) {
            return RestResult.fail("修改角色信息失败");
        }

        if (!rolePermissionRepository.update(role.getId(), permissions)) {
            return RestResult.fail("修改角色权限失败");
        }
        return RestResult.ok();
    }

    public RestResult del(int id, int rid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_rolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        TRole role = roleRepository.find(rid);
        if (null == role) {
            return RestResult.fail("要删除的角色不存在");
        }

        if (!rolePermissionRepository.delete(rid)) {
            return RestResult.fail("删除角色权限失败");
        }
        if (!roleRepository.delete(rid)) {
            return RestResult.fail("删除角色信息失败");
        }
        return RestResult.ok();
    }

    public RestResult get(int id, int rid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_rolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        TRole role = roleRepository.find(rid);
        if (null == role) {
            return RestResult.fail("获取角色失败");
        }

        List<Integer> permissions = rolePermissionRepository.find(rid);
        if (null == permissions) {
            return RestResult.fail("获取角色权限失败");
        }
        val data = new HashMap<String, Object>();
        data.put("role", role);
        data.put("permissions", permissions);
        return RestResult.ok(data);
    }

    public RestResult getList(int id, String search) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_rolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        List<TRole> roles = roleRepository.all(search);
        if (null == roles) {
            return RestResult.fail("获取角色信息失败");
        }
        val ret = new HashMap<String, Object>();
        ret.put("roles", roles);
        return RestResult.ok(ret);
    }

    public RestResult getUserRole(int id, int uid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_rolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        TUser user = userRepository.find(uid);
        if (null == user) {
            return RestResult.fail("用户信息失败");
        }
        TRole role = roleRepository.find(user.getRid());
        if (null == role) {
            return RestResult.fail("获取角色信息失败");
        }
        return RestResult.ok(role);
    }

    public RestResult setUserRole(int id, int uid, int rid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_rolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 已存在就修改，不存在就新增
        TUser user = userRepository.find(uid);
        if (null == user) {
            return RestResult.fail("关联角色失败");
        }
        user.setRid(rid);
        if (!userRepository.update(user)) {
            return RestResult.fail("修改关联角色失败");
        }
        return RestResult.ok();
    }
}
