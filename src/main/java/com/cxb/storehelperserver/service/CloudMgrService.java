package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;

/**
 * desc: 云仓业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CloudMgrService {
    @Resource
    private CheckService checkService;

    @Resource
    private CloudRepository cloudRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addCloud(int id, TCloud cloud) {
        // 验证公司
        String msg = checkService.checkGroup(id, cloud.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 云仓名重名检测
        if (cloudRepository.check(cloud.getGid(), cloud.getName(), 0)) {
            return RestResult.fail("云仓名称已存在");
        }

        if (!cloudRepository.insert(cloud)) {
            return RestResult.fail("添加云仓信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setCloud(int id, TCloud cloud) {
        // 验证公司
        String msg = checkService.checkGroup(id, cloud.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 云仓名重名检测
        if (cloudRepository.check(cloud.getGid(), cloud.getName(), cloud.getId())) {
            return RestResult.fail("云仓名称已存在");
        }

        if (!cloudRepository.update(cloud)) {
            return RestResult.fail("修改云仓信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delCloud(int id, int gid, int sid) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, stock_cloudaddress)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // TODO 检验是否存在库存

        if (!cloudRepository.delete(sid)) {
            return RestResult.fail("删除云仓信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupCloud(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = cloudRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        val list2 = new ArrayList<>();
        val list = cloudRepository.pagination(group.getGid(), page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TCloud g : list) {
                val cloud = new HashMap<String, Object>();
                cloud.put("id", g.getId());
                cloud.put("area", String.valueOf(g.getArea()));
                cloud.put("name", g.getName());
                cloud.put("address", g.getAddress());
                cloud.put("contact", userRepository.find(g.getContact()));
                list2.add(cloud);
            }
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getGroupAllCloud(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = cloudRepository.total(group.getGid(), null);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        val list2 = new ArrayList<>();
        val list = cloudRepository.pagination(group.getGid(), 1, total, null);
        if (null != list && !list.isEmpty()) {
            for (TCloud g : list) {
                val cloud = new HashMap<String, Object>();
                cloud.put("id", g.getId());
                cloud.put("area", String.valueOf(g.getArea()));
                cloud.put("name", g.getName());
                cloud.put("address", g.getAddress());
                cloud.put("contact", userRepository.find(g.getContact()));
                list2.add(cloud);
            }
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }
}
