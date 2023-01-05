package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.GroupRepository;
import com.cxb.storehelperserver.repository.RoleRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.repository.UserRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.ArrayList;
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
    private CheckService checkService;

    @Resource
    private GroupRepository groupRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private RoleRepository roleRepository;

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
        if (!checkService.checkRolePermission(id, admin_grouplist)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        // 检查是否存在关联员工
        if (userGroupRepository.checkUser(gid)) {
            return RestResult.fail("删除公司失败，还存在关联的员工");
        }

        // 检查是否存在关联角色
        if (roleRepository.check(gid, null)) {
            return RestResult.fail("删除公司失败，还存在关联的角色");
        }

        // 公司是用软删除
        if (!groupRepository.delete(gid)) {
            return RestResult.fail("删除公司信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupList(int id, int page, int limit, String search) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, admin_grouplist)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        int total = groupRepository.total(search);
        if (0 == total) {
            return RestResult.fail("没有查询到任何公司信息");
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);

        // 查询联系人
        val list = groupRepository.pagination(page, limit, search);
        if (null != list && !list.isEmpty()) {
            val list2 = new ArrayList<>();
            for (TGroup g : list) {
                val group = new HashMap<String, Object>();
                group.put("id", g.getId());
                group.put("name", g.getName());
                group.put("address", g.getAddress());
                group.put("contact", userRepository.find(g.getContact()));
                list2.add(group);
            }
            data.put("list", list2);
        } else {
            data.put("list", null);
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
        if (!checkService.checkRolePermission(id, system_rolelist)) {
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
        return RestResult.ok();
    }

    public RestResult setUserGroupAdmin(int id, int uid, int gid) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, admin)) {
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
        return RestResult.ok();
    }
}
