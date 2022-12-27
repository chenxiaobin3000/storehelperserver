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
        String msg = checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // TODO 角色名重名检测

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
        String msg = checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // TODO 角色名重名检测

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
        String msg = checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!rolePermissionRepository.delete(id)) {
            return RestResult.fail("修改角色权限失败");
        }
        if (!roleRepository.delete(id)) {
            return RestResult.fail("删除角色信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getRole(int id, int rid) {
        TRole role = roleRepository.find(rid);
        if (null == role) {
            return RestResult.fail("要删除的角色不存在");
        }

        // 验证公司
        String msg = checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        List<Integer> permissions = rolePermissionRepository.find(rid);
        if (null == permissions) {
            return RestResult.fail("获取角色权限失败");
        }
        val data = new HashMap<String, Object>();
        data.put("role", role);
        data.put("permission", permissions);
        return RestResult.ok(data);
    }

    public RestResult getRoleList(int id, int gid) {
        // 验证公司
        String msg = checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        List<TRole> roles = roleRepository.all(gid);
        if (null == roles) {
            return RestResult.fail("获取角色信息失败");
        }
        val ret = new HashMap<String, Object>();
        ret.put("roles", roles);
        return RestResult.ok(ret);
    }

    public RestResult getUserRole(int id, int uid) {
        // 操作员必须同公司用户
        TUserGroup group = userGroupRepository.find(id);
        TUserGroup group2 = userGroupRepository.find(uid);
        if (null == group || null == group2 || !group2.getGid().equals(group.getGid())) {
            return RestResult.fail("操作仅限本公司");
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
        TUserGroup group = userGroupRepository.find(id);
        TUserGroup group2 = userGroupRepository.find(uid);
        if (null == group || null == group2 || !group2.getGid().equals(group.getGid())) {
            return RestResult.fail("操作仅限本公司");
        }

        // 权限校验
        if (!checkRolePermission(id, system_getrolelist)) {
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
        if (checkRolePermission(id, admin)) {
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

    public boolean checkRolePermission(int uid, int permission) {
        List<Integer> permissions = rolePermissionRepository.find(uid);
        if (null != permissions) {
            for (Integer p1 : permissions) {
                if (p1.equals(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkRolePermission(int uid, List<Integer> permissions) {
        List<Integer> userPermissions = rolePermissionRepository.find(uid);
        if (null != userPermissions) {
            for (Integer p1 : userPermissions) {
                for (Integer p2 : permissions) {
                    if (p1.equals(p2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String checkGroup(int uid, int gid) {
        TUserGroup userGroup = userGroupRepository.find(uid);
        if (null == userGroup) {
            return "未查询到关联的公司";
        }
        TGroup group = groupRepository.find(userGroup.getGid());
        if (null == group) {
            return "未查询到关联公司的信息";
        }
        if (!group.getId().equals(gid)) {
            return "操作仅限本公司员工";
        }
        return null;
    }
}
