package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TOrderReviewer;
import com.cxb.storehelperserver.model.TRoleMp;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.model.TUserRoleMp;
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
 * desc: 小程序角色业务
 * auth: cxb
 * date: 2023/1/17
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleMpService {
    @Resource
    private CheckService checkService;

    @Resource
    private RoleMpRepository roleMpRepository;

    @Resource
    private RolePermissionMpRepository rolePermissionMpRepository;

    @Resource
    private UserRoleMpRepository userRoleMpRepository;

    @Resource
    private OrderReviewerRepository orderReviewerRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private GroupRepository groupRepository;

    public RestResult addRoleMp(int id, TRoleMp roleMp, List<Integer> permissions) {
        // 验证公司
        String msg = checkService.checkGroup(id, roleMp.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 角色名重名检测
        if (roleMpRepository.check(roleMp.getGid(), roleMp.getName(), 0)) {
            return RestResult.fail("角色名称已存在");
        }

        if (!roleMpRepository.insert(roleMp)) {
            return RestResult.fail("添加角色信息失败");
        }
        if (!rolePermissionMpRepository.insert(roleMp.getId(), permissions)) {
            return RestResult.fail("添加角色权限失败");
        }
        if (!syncReviewPerm(roleMp.getGid())) {
            return RestResult.fail("同步角色权限信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setRoleMp(int id, TRoleMp role, List<Integer> permissions) {
        // 验证公司
        String msg = checkService.checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 角色名重名检测
        if (roleMpRepository.check(role.getGid(), role.getName(), role.getId())) {
            return RestResult.fail("角色名称已存在");
        }

        if (!roleMpRepository.update(role)) {
            return RestResult.fail("修改角色信息失败");
        }
        if (!rolePermissionMpRepository.update(role.getId(), permissions)) {
            return RestResult.fail("修改角色权限失败");
        }
        if (!syncReviewPerm(role.getGid())) {
            return RestResult.fail("同步角色权限信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delRoleMp(int id, int rid) {
        TRoleMp role = roleMpRepository.find(rid);
        if (null == role) {
            return RestResult.fail("要删除的角色不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!rolePermissionMpRepository.delete(rid)) {
            return RestResult.fail("删除角色权限失败");
        }
        if (!roleMpRepository.delete(rid)) {
            return RestResult.fail("删除角色信息失败");
        }
        if (!syncReviewPerm(role.getGid())) {
            return RestResult.fail("同步角色权限信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getRoleMp(int id, int rid) {
        TRoleMp role = roleMpRepository.find(rid);
        if (null == role) {
            return RestResult.fail("获取角色失败");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, role.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        List<Integer> permissions = rolePermissionMpRepository.find(rid);
        if (null == permissions) {
            return RestResult.fail("获取角色权限失败");
        }
        val data = new HashMap<String, Object>();
        data.put("role", role);
        data.put("permissions", permissions);
        return RestResult.ok(data);
    }

    public RestResult getRoleMpList(int id, int gid, String search) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        List<TRoleMp> roles = roleMpRepository.all(gid, search);
        if (null == roles) {
            return RestResult.fail("获取角色信息失败");
        }
        val ret = new HashMap<String, Object>();
        ret.put("roles", roles);
        return RestResult.ok(ret);
    }

    public RestResult getUserRoleMp(int id, int uid) {
        // 操作员必须同公司用户
        String msg = checkService.checkSampGroup(id, uid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        TUserRoleMp userRoleMp = userRoleMpRepository.find(uid);
        if (null == userRoleMp) {
            return RestResult.fail("用户信息失败");
        }
        TRoleMp role = roleMpRepository.find(userRoleMp.getRid());
        if (null == role) {
            return RestResult.fail("获取角色信息失败");
        }
        return RestResult.ok(role);
    }

    public RestResult setUserRoleMp(int id, int uid, int rid) {
        // 操作员必须同公司用户
        TUserGroup group = userGroupRepository.find(uid);
        if (null == group) {
            return RestResult.fail("未查询到用户关联的公司");
        }
        String msg = checkService.checkGroup(id, group.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 权限校验
        if (!checkService.checkRolePermission(id, system_mprolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // rid为0就删除
        if (0 == rid) {
            TUserRoleMp role = userRoleMpRepository.find(uid);
            if (null != role) {
                userRoleMpRepository.delete(role);
            }
        } else {
            // 已存在就修改，不存在就新增
            TUserRoleMp role = userRoleMpRepository.find(uid);
            if (null == role) {
                role = new TUserRoleMp();
                role.setUid(uid);
                role.setRid(rid);
                if (!userRoleMpRepository.insert(role)) {
                    return RestResult.fail("关联角色失败");
                }
            } else {
                role.setRid(rid);
                if (!userRoleMpRepository.update(role)) {
                    return RestResult.fail("修改关联角色失败");
                }
            }
        }

        if (!syncReviewPerm(group.getGid())) {
            return RestResult.fail("同步角色权限信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupRoleMp(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        List<TRoleMp> roles = roleMpRepository.findByGroup(group.getGid());
        if (null == roles) {
            return RestResult.fail("获取角色信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("list", roles);
        return RestResult.ok(data);
    }

    private boolean syncReviewPerm(int gid) {
        val perms = userRoleMpRepository.getUserRoleMpPerms(gid, mp_purchase_review, mp_end_review);
        for (TOrderReviewer orderReviewer : perms) {
            orderReviewer.setId(0);
        }
        return orderReviewerRepository.update(perms, gid);
    }
}
