package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TSupplier;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.PurchaseOrderRepository;
import com.cxb.storehelperserver.repository.StorageOrderRepository;
import com.cxb.storehelperserver.repository.SupplierRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.service.model.PageData;
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

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addSupplier(int id, TSupplier supplier) {
        // 验证公司
        String msg = checkService.checkGroup(id, supplier.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 供应商名重名检测
        if (supplierRepository.check(supplier.getGid(), supplier.getName(), 0)) {
            return RestResult.fail("供应商名称已存在");
        }

        if (!supplierRepository.insert(supplier)) {
            return RestResult.fail("添加供应商信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setSupplier(int id, TSupplier supplier) {
        // 验证公司
        String msg = checkService.checkGroup(id, supplier.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 供应商名重名检测
        if (supplierRepository.check(supplier.getGid(), supplier.getName(), supplier.getId())) {
            return RestResult.fail("供应商名称已存在");
        }

        if (!supplierRepository.update(supplier)) {
            return RestResult.fail("修改供应商信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delSupplier(int id, int gid, int sid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_supplier)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 检验是否存在各种订单
        if (purchaseOrderRepository.checkBySupplier(sid)) {
            return RestResult.fail("供应商还存在采购订单");
        }
        if (!supplierRepository.delete(sid)) {
            return RestResult.fail("删除供应商信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupSupplier(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = supplierRepository.total(group.getGid(), search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = supplierRepository.pagination(group.getGid(), page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TSupplier g : list) {
                val supplier = new HashMap<String, Object>();
                supplier.put("id", g.getId());
                supplier.put("name", g.getName());
                supplier.put("contact", g.getContact());
                supplier.put("phone", g.getPhone());
                supplier.put("remark", g.getRemark());
                list2.add(supplier);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getGroupAllSupplier(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = supplierRepository.total(group.getGid(), null);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = supplierRepository.pagination(group.getGid(), 1, total, null);
        if (null != list && !list.isEmpty()) {
            for (TSupplier s : list) {
                val supplier = new HashMap<String, Object>();
                supplier.put("id", s.getId());
                supplier.put("name", s.getName());
                supplier.put("contact", s.getContact());
                supplier.put("phone", s.getPhone());
                supplier.put("remark", s.getRemark());
                list2.add(supplier);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }
}
