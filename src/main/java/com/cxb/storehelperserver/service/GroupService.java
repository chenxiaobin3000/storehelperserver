package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.GroupRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.cxb.storehelperserver.config.Permission.*;

/**
 * desc: 公司业务
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class GroupService {
    @Resource
    private RoleService roleService;

    @Resource
    private GroupRepository groupRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addGroup(TGroup group) {
        if (!groupRepository.insert(group)) {
            return RestResult.fail("添加公司信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setGroup(TGroup group) {
        if (!groupRepository.update(group)) {
            return RestResult.fail("修改公司信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delGroup(int id) {
        if (!groupRepository.delete(id)) {
            return RestResult.fail("删除公司信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getUserGroup(int uid) {
        TUserGroup userGroup = userGroupRepository.find(uid);
        if (null == userGroup) {
            return RestResult.fail("用户信息异常");
        }
        TGroup group = groupRepository.find(userGroup.getGid());
        if (null == group) {
            return RestResult.fail("公司信息异常");
        }
        return RestResult.ok(group);
    }

    public RestResult setUserGroup(int id, int uid, int gid) {
        // 操作员必须同公司用户
        TUserGroup group = userGroupRepository.find(id);
        if (!group.getGid().equals(gid)) {
            return RestResult.fail("操作仅限本公司");
        }

        // 权限校验
        if (!roleService.checkRolePermission(id, system_getrolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 已存在就修改，不存在就新增
        group = userGroupRepository.find(uid);
        if (null == group) {
            group = new TUserGroup();
            group.setUid(uid);
            group.setGid(gid);
            if (!userGroupRepository.insert(group)) {
                return RestResult.fail("关联公司失败");
            }
        } else {
            group.setGid(gid);
            if (!userGroupRepository.update(group)) {
                return RestResult.fail("修改关联公司失败");
            }
        }

        // 修改以用户id缓存的公司信息
        groupRepository.updateByUid(uid, gid);
        return RestResult.ok();
    }

    public RestResult setUserGroupAdmin(int id, int uid, int gid) {
        // 权限校验，必须admin
        if (!roleService.checkRolePermission(id, admin)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        // 已存在就修改，不存在就新增
        TUserGroup group = userGroupRepository.find(uid);
        if (null == group) {
            group = new TUserGroup();
            group.setUid(uid);
            group.setGid(gid);
            if (!userGroupRepository.insert(group)) {
                return RestResult.fail("关联公司失败");
            }
        } else {
            group.setGid(gid);
            if (!userGroupRepository.update(group)) {
                return RestResult.fail("修改关联公司失败");
            }
        }

        // 修改以用户id缓存的公司信息
        groupRepository.updateByUid(uid, gid);
        return RestResult.ok();
    }
}
