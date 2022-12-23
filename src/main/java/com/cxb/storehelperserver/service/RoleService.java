package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TRole;
import com.cxb.storehelperserver.model.TUserRole;
import com.cxb.storehelperserver.repository.GroupRepository;
import com.cxb.storehelperserver.repository.RoleRepository;
import com.cxb.storehelperserver.repository.UserRoleRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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
    private UserRoleRepository userRoleRepository;

    @Resource
    private GroupRepository groupRepository;

    public RestResult addRole(TRole role) {
        // 验证公司
        TGroup group = groupRepository.find(role.getGid());
        if (null == group) {
            RestResult.fail("未查询到关联公司的信息");
        }

        boolean ret = roleRepository.insert(role);
        if (!ret) {
            RestResult.fail("添加角色信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setRole(TRole role) {
        boolean ret = roleRepository.update(role);
        if (!ret) {
            RestResult.fail("修改角色信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delRole(int id) {
        boolean ret = roleRepository.delete(id);
        if (!ret) {
            RestResult.fail("删除角色信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getUserRole(int uid) {
        TUserRole userRole = userRoleRepository.find(uid);
        if (null == userRole) {
            return RestResult.fail("用户信息异常");
        }
        TRole role = roleRepository.find(userRole.getRid());
        if (null == role) {
            return RestResult.fail("角色信息异常");
        }
        return RestResult.ok(role);
    }

    public RestResult setUserRole(int uid, int rid) {
        return RestResult.ok();
    }
}
