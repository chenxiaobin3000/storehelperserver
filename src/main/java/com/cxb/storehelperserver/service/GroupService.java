package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
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
    private GroupMarketRepository groupMarketRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private RolePermissionRepository rolePermissionRepository;

    @Resource
    private AccountRepository accountRepository;

    @Value("${store-app.config.defaultpwd}")
    private String defaultpwd;

    public RestResult addGroup(int id, String account, String phone, TGroup group, List<Integer> markets) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, admin_grouplist)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        TUser user = userRepository.insert(account, phone);
        if (null == user) {
            return RestResult.fail("新增用户信息失败");
        }
        int uid = user.getId();

        group.setContact(uid);
        if (!groupRepository.insert(group)) {
            return RestResult.fail("添加公司信息失败");
        }
        int gid = group.getId();

        if (!groupMarketRepository.update(gid, markets)) {
            return RestResult.fail("新增公司对接平台信息失败");
        }

        TRole role = new TRole();
        role.setGid(gid);
        role.setName("默认角色");
        if (!roleRepository.insert(role)) {
            return RestResult.fail("添加角色信息失败");
        }
        int rid = role.getId();

        val permissions = new ArrayList<Integer>();
        permissions.add(system);
        permissions.add(system_rolelist);
        if (!rolePermissionRepository.insert(rid, permissions)) {
            return RestResult.fail("添加角色权限失败");
        }

        if (!userRoleRepository.insert(uid, rid)) {
            return RestResult.fail("新增用户角色信息失败");
        }

        if (!userGroupRepository.insert(uid, gid)) {
            return RestResult.fail("新增用户公司信息失败");
        }

        if (!accountRepository.insert(account, defaultpwd, uid)) {
            return RestResult.fail("新增账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setGroup(int id, TGroup group, List<Integer> markets) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, admin_grouplist)) {
            return RestResult.fail("本账号没有管理员权限");
        }
        TGroup group1 = groupRepository.find(group.getId());
        if (null == group1) {
            return RestResult.fail("获取公司信息失败");
        }
        group.setMoney(group1.getMoney());
        if (!groupRepository.update(group)) {
            return RestResult.fail("修改公司信息失败");
        }
        if (!groupMarketRepository.update(group.getId(), markets)) {
            return RestResult.fail("修改公司对接平台信息失败");
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
        if (roleRepository.check(gid, null, 0)) {
            return RestResult.fail("删除公司失败，还存在关联的角色");
        }

        // 公司是用软删除
        if (!groupRepository.delete(gid)) {
            return RestResult.fail("删除公司信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroup(int id, int gid) {
        TUserGroup userGroup = userGroupRepository.find(id);
        if (null == userGroup) {
            return RestResult.fail("未查询到关联的公司");
        }
        TGroup group = groupRepository.find(userGroup.getGid());
        if (null == group) {
            return RestResult.fail("未查询到关联公司的信息");
        }
        if (!group.getId().equals(gid)) {
            return RestResult.fail("操作仅限本公司员工");
        }
        val data = new HashMap<String, Object>();
        data.put("name", group.getName());
        data.put("area", String.valueOf(group.getArea()));
        data.put("address", group.getAddress());
        data.put("money", group.getMoney());

        TUser user = userRepository.find(group.getContact());
        if (null == user) {
            data.put("contact", group.getContact());
        } else {
            data.put("contact", user.getName());
        }
        return RestResult.ok(data);
    }

    public RestResult getGroupList(int id, int page, int limit, String search) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, admin_grouplist)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        int total = groupRepository.total(search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val list = groupRepository.pagination(page, limit, search);
        if (null == list) {
            return RestResult.fail("未查询到公司信息");
        }

        // 查询联系人
        val list2 = new ArrayList<>();
        for (TGroup g : list) {
            val group = new HashMap<String, Object>();
            group.put("id", g.getId());
            group.put("area", String.valueOf(g.getArea()));
            group.put("name", g.getName());
            group.put("address", g.getAddress());
            group.put("contact", userRepository.find(g.getContact()));
            group.put("money", g.getMoney());
            group.put("market", groupMarketRepository.find(g.getId()));
            list2.add(group);
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
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
            if (!userGroupRepository.insert(uid, gid)) {
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
