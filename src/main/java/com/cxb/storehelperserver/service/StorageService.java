package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.PageData;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;

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

    public RestResult add(int id, TStorage storage) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_storage)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 仓库名重名检测
        if (storageRepository.check(storage.getName(), 0)) {
            return RestResult.fail("仓库名称已存在");
        }

        if (!storageRepository.insert(storage)) {
            return RestResult.fail("添加仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult set(int id, TStorage storage) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_storage)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 仓库名重名检测
        if (storageRepository.check(storage.getName(), storage.getId())) {
            return RestResult.fail("仓库名称已存在");
        }

        if (!storageRepository.update(storage)) {
            return RestResult.fail("修改仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult del(int id, int sid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_storage)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }
        if (!storageRepository.delete(sid)) {
            return RestResult.fail("删除仓库信息失败");
        }
        return RestResult.ok();
    }

    public RestResult get(int id, int page, int limit, String search) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_storage)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        int total = storageRepository.total(search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = storageRepository.pagination(page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TStorage g : list) {
                val storage = new HashMap<String, Object>();
                storage.put("id", g.getId());
                storage.put("area", String.valueOf(g.getArea()));
                storage.put("name", g.getName());
                storage.put("address", g.getAddress());
                list2.add(storage);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getAll(int id) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_storage)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        int total = storageRepository.total(null);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = storageRepository.pagination(1, total, null);
        if (null != list && !list.isEmpty()) {
            for (TStorage s : list) {
                val storage = new HashMap<String, Object>();
                storage.put("id", s.getId());
                storage.put("area", String.valueOf(s.getArea()));
                storage.put("name", s.getName());
                storage.put("address", s.getAddress());
                list2.add(storage);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }
}
