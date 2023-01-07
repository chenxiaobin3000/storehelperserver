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

import static com.cxb.storehelperserver.config.Permission.*;

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
    private UserRoleRepository userRoleRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private GroupRepository groupRepository;

    public RestResult addRole(int id, TRole role, List<Integer> permissions) {
        // 验证公司
        String msg = checkService.checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 角色名重名检测
        if (roleRepository.check(role.getGid(), role.getName(), 0)) {
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

    public RestResult setRole(int id, TRole role, List<Integer> permissions) {
        // 验证公司
        String msg = checkService.checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 角色名重名检测
        if (roleRepository.check(role.getGid(), role.getName(), id)) {
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

    public RestResult delRole(int id, int rid) {
        TRole role = roleRepository.find(rid);
        if (null == role) {
            return RestResult.fail("要删除的角色不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!rolePermissionRepository.delete(rid)) {
            return RestResult.fail("修改角色权限失败");
        }
        if (!roleRepository.delete(rid)) {
            return RestResult.fail("删除角色信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getRole(int id, int rid) {
        TRole role = roleRepository.find(rid);
        if (null == role) {
            return RestResult.fail("获取角色失败");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
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

    public RestResult getRoleList(int id, int gid, String search) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        List<TRole> roles = roleRepository.all(gid, search);
        if (null == roles) {
            return RestResult.fail("获取角色信息失败");
        }
        val ret = new HashMap<String, Object>();
        ret.put("roles", roles);
        return RestResult.ok(ret);
    }

    public RestResult getUserRole(int id, int uid) {
        // 操作员必须同公司用户
        String msg = checkService.checkSampGroup(id, uid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        TUserRole userRole = userRoleRepository.find(uid);
        if (null == userRole) {
            return RestResult.fail("用户信息异常");
        }
        TRole role = roleRepository.find(userRole.getRid());
        if (null == role) {
            return RestResult.fail("获取角色信息异常");
        }
        return RestResult.ok(role);
    }

    public RestResult setUserRole(int id, int uid, int rid) {
        // 操作员必须同公司用户
        String msg = checkService.checkSampGroup(id, uid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 权限校验
        if (!checkService.checkRolePermission(id, system_rolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 已存在就修改，不存在就新增
        TUserRole role = userRoleRepository.find(uid);
        if (null == role) {
            role = new TUserRole();
            role.setUid(uid);
            role.setRid(rid);
            if (!userRoleRepository.insert(role)) {
                return RestResult.fail("关联角色失败");
            }
        } else {
            role.setRid(rid);
            if (!userRoleRepository.update(role)) {
                return RestResult.fail("修改关联角色失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult setUserRoleAdmin(int id, int uid, int rid) {
        // 权限校验，必须admin
        if (checkService.checkRolePermission(id, admin)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        // 已存在就修改，不存在就新增
        TUserRole role = userRoleRepository.find(uid);
        if (null == role) {
            role = new TUserRole();
            role.setUid(uid);
            role.setRid(rid);
            if (!userRoleRepository.insert(role)) {
                return RestResult.fail("关联角色失败");
            }
        } else {
            role.setRid(rid);
            if (!userRoleRepository.update(role)) {
                return RestResult.fail("修改关联角色失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult getGroupRole(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        List<TRole> roles = roleRepository.findByGroup(group.getGid());
        if (null == roles) {
            return RestResult.fail("获取角色信息异常");
        }
        val data = new HashMap<String, Object>();
        data.put("list", roles);
        return RestResult.ok(data);
    }
}
