package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TSupplier;
import com.cxb.storehelperserver.repository.SupplierRepository;
import com.cxb.storehelperserver.util.PageData;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

import static com.cxb.storehelperserver.util.Permission.system_supplier;

/**
 * desc: 供应商业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SupplierService {
    @Resource
    private CheckService checkService;

    @Resource
    private SupplierRepository supplierRepository;

    public RestResult add(int id, TSupplier supplier) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_supplier)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 供应商名重名检测
        if (supplierRepository.check(supplier.getName(), 0)) {
            return RestResult.fail("供应商名称已存在");
        }

        if (!supplierRepository.insert(supplier)) {
            return RestResult.fail("添加供应商信息失败");
        }
        return RestResult.ok();
    }

    public RestResult set(int id, TSupplier supplier) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_supplier)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 供应商名重名检测
        if (supplierRepository.check(supplier.getName(), supplier.getId())) {
            return RestResult.fail("供应商名称已存在");
        }

        if (!supplierRepository.update(supplier)) {
            return RestResult.fail("修改供应商信息失败");
        }
        return RestResult.ok();
    }

    public RestResult del(int id, int sid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_supplier)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }
        if (!supplierRepository.delete(sid)) {
            return RestResult.fail("删除供应商信息失败");
        }
        return RestResult.ok();
    }

    public RestResult get(int id, int page, int limit, String search) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_supplier)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        int total = supplierRepository.total(search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = supplierRepository.pagination(page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TSupplier g : list) {
                val supplier = new HashMap<String, Object>();
                supplier.put("id", g.getId());
                supplier.put("name", g.getName());
                supplier.put("phone", g.getPhone());
                list2.add(supplier);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getAll(int id) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_supplier)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        int total = supplierRepository.total(null);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = supplierRepository.pagination(1, total, null);
        if (null != list && !list.isEmpty()) {
            for (TSupplier s : list) {
                val supplier = new HashMap<String, Object>();
                supplier.put("id", s.getId());
                supplier.put("name", s.getName());
                supplier.put("phone", s.getPhone());
                list2.add(supplier);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }
}
