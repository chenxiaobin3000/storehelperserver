package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.model.TUserRole;
import com.cxb.storehelperserver.model.TUserRoleMp;
import com.cxb.storehelperserver.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 校验业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CheckService {
    @Resource
    private RolePermissionRepository rolePermissionRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    @Resource
    private RolePermissionMpRepository rolePermissionMpRepository;

    @Resource
    private UserRoleMpRepository userRoleMpRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private GroupRepository groupRepository;

    /*
     * desc: 判断 id 是否在 gid 公司
     */
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

    /*
     * desc: 判断 id 和 uid 是否在同一公司
     */
    public String checkSampGroup(int id, int uid) {
        TUserGroup group = userGroupRepository.find(id);
        TUserGroup group2 = userGroupRepository.find(uid);
        if (null == group || null == group2 || !group2.getGid().equals(group.getGid())) {
            return "操作仅限本公司";
        }
        return null;
    }

    public boolean checkRolePermission(int uid, int permission) {
        TUserRole userRole = userRoleRepository.find(uid);
        if (null == userRole) {
            return false;
        }
        List<Integer> permissions = rolePermissionRepository.find(userRole.getRid());
        if (null != permissions) {
            for (Integer p1 : permissions) {
                if (p1.equals(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkRolePermissionMp(int uid, int permission) {
        TUserRoleMp userRoleMp = userRoleMpRepository.find(uid);
        if (null == userRoleMp) {
            return false;
        }
        List<Integer> permissions = rolePermissionMpRepository.find(userRoleMp.getRid());
        if (null != permissions) {
            for (Integer p1 : permissions) {
                if (p1.equals(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}
