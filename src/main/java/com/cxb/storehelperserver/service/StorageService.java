package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.repository.StorageRepository;
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

    public RestResult addStorage(TStorage storage) {
        if (!storageRepository.insert(storage)) {
            return RestResult.fail("添加仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setStorage(TStorage storage) {
        if (!storageRepository.update(storage)) {
            return RestResult.fail("修改仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delStorage(int id, int gid) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, storage_address)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        // 检查是否存在关联员工
        if (userStorageRepository.checkUser(gid)) {
            return RestResult.fail("删除仓库失败，还存在关联的员工");
        }

        // 检查是否存在关联角色
        if (roleRepository.check(gid, null, 0)) {
            return RestResult.fail("删除仓库失败，还存在关联的角色");
        }

        // 仓库是用软删除
        if (!storageRepository.delete(gid)) {
            return RestResult.fail("删除仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getStorageList(int id, int page, int limit, String search) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, admin_storagelist)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        int total = storageRepository.total(search);
        if (0 == total) {
            return RestResult.fail("未查询到任何仓库信息");
        }

        // 查询联系人
        val list2 = new ArrayList<>();
        val list = storageRepository.pagination(page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TStorage g : list) {
                val storage = new HashMap<String, Object>();
                storage.put("id", g.getId());
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

    public RestResult setUserStorage(int id, int uid, int gid) {
        // 操作员必须同仓库用户
        TUserStorage storage = userStorageRepository.find(id);
        if (!storage.getGid().equals(gid)) {
            return RestResult.fail("操作仅限本仓库");
        }

        // 权限校验
        if (!checkService.checkRolePermission(id, system_rolelist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 已存在就修改，不存在就新增
        storage = userStorageRepository.find(uid);
        if (null == storage) {
            storage = new TUserStorage();
            storage.setUid(uid);
            storage.setGid(gid);
            if (!userStorageRepository.insert(storage)) {
                return RestResult.fail("关联仓库失败");
            }
        } else {
            storage.setGid(gid);
            if (!userStorageRepository.update(storage)) {
                return RestResult.fail("修改关联仓库失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult setUserStorageAdmin(int id, int uid, int gid) {
        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, admin)) {
            return RestResult.fail("本账号没有管理员权限");
        }

        // 已存在就修改，不存在就新增
        TUserStorage storage = userStorageRepository.find(uid);
        if (null == storage) {
            storage = new TUserStorage();
            storage.setUid(uid);
            storage.setGid(gid);
            if (!userStorageRepository.insert(storage)) {
                return RestResult.fail("关联仓库失败");
            }
        } else {
            storage.setGid(gid);
            if (!userStorageRepository.update(storage)) {
                return RestResult.fail("修改关联仓库失败");
            }
        }
        return RestResult.ok();
    }
}
