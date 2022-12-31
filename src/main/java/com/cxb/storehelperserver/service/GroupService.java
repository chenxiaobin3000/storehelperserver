package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.GroupRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.repository.UserRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.HashMap;

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

    @Resource
    private UserRepository userRepository;

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

    public RestResult delGroup(int id, int gid) {
        // 权限校验，必须admin
        if (!roleService.checkRolePermission(id, admin_grouplist)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        // TODO 检查是否存在关联员工

        // TODO 检查是否存在关联角色

        // 公司是用软删除
        if (!groupRepository.delete(gid)) {
            return RestResult.fail("删除公司信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupList(int id, int page, int limit) {
        // 权限校验，必须admin
        if (!roleService.checkRolePermission(id, admin_grouplist)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        int total = groupRepository.total();
        if (0 == total) {
            return RestResult.fail("没有查询到任何公司信息");
        }

        val list = groupRepository.pagination(page, limit);
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list);

        // 查询联系人
        if (null != list && !list.isEmpty()) {
            val contacts = new HashMap<Integer, TUser>();
            for (TGroup g : list) {
                TUser user = userRepository.find(g.getContact());
                if (null != user) {
                    contacts.put(user.getId(), user);
                }
            }
            data.put("contacts", contacts);
        }
        return RestResult.ok(data);
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
