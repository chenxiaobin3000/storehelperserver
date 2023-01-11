package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.*;
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
 * desc: 仓库业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageService {
    @Resource
    private CheckService checkService;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private ScInOrderRepository scInOrderRepository;

    @Resource
    private ScOutOrderRepository scOutOrderRepository;

    @Resource
    private SoInOrderRepository soInOrderRepository;

    @Resource
    private SoOutOrderRepository soOutOrderRepository;

    public RestResult addStorage(int id, TStorage storage) {
        // 验证公司
        String msg = checkService.checkGroup(id, storage.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 仓库名重名检测
        if (storageRepository.check(storage.getGid(), storage.getName(), 0)) {
            return RestResult.fail("仓库名称已存在");
        }

        if (!storageRepository.insert(storage)) {
            return RestResult.fail("添加仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setStorage(int id, TStorage storage) {
        // 验证公司
        String msg = checkService.checkGroup(id, storage.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 仓库名重名检测
        if (storageRepository.check(storage.getGid(), storage.getName(), storage.getId())) {
            return RestResult.fail("仓库名称已存在");
        }

        if (!storageRepository.update(storage)) {
            return RestResult.fail("修改仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delStorage(int id, int gid, int sid) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, storage_address)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 是否存在出入库信息
        if (scInOrderRepository.check(gid)) {
            return RestResult.fail("删除失败，仓库还存在商品入库订单！");
        }
        if (scOutOrderRepository.check(gid)) {
            return RestResult.fail("删除失败，仓库还存在商品出库订单！");
        }
        if (soInOrderRepository.check(gid)) {
            return RestResult.fail("删除失败，仓库还存在原料入库订单！");
        }
        if (soOutOrderRepository.check(gid)) {
            return RestResult.fail("删除失败，仓库还存在原料出库订单！");
        }

        if (!storageRepository.delete(sid)) {
            return RestResult.fail("删除仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupStorage(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        int total = storageRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        val list2 = new ArrayList<>();
        val list = storageRepository.pagination(group.getGid(), page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TStorage g : list) {
                val storage = new HashMap<String, Object>();
                storage.put("id", g.getId());
                storage.put("area", String.valueOf(g.getArea()));
                storage.put("name", g.getName());
                storage.put("address", g.getAddress());
                storage.put("contact", userRepository.find(g.getContact()));
                list2.add(storage);
            }
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }
}
