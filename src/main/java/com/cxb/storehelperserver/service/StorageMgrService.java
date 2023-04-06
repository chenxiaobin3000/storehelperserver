package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.service.model.PageData;
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
public class StorageMgrService {
    @Resource
    private CheckService checkService;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

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
        if (!checkService.checkRolePermission(id, system_storageaddress)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 检验是否存在各种订单
        if (purchaseOrderRepository.check(sid)) {
            return RestResult.fail("仓库还存在采购订单");
        }
        if (agreementOrderRepository.check(sid)) {
            return RestResult.fail("仓库还存在履约订单");
        }
        if (productOrderRepository.check(sid)) {
            return RestResult.fail("仓库还存在生产订单");
        }
        if (storageOrderRepository.check(sid)) {
            return RestResult.fail("仓库还存在入库订单");
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
            return RestResult.fail("获取公司信息失败");
        }

        int total = storageRepository.total(group.getGid(), search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = storageRepository.pagination(group.getGid(), page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TStorage g : list) {
                val storage = new HashMap<String, Object>();
                storage.put("id", g.getId());
                storage.put("area", String.valueOf(g.getArea()));
                storage.put("name", g.getName());
                storage.put("address", g.getAddress());
                storage.put("contact", g.getContact());
                storage.put("phone", g.getPhone());
                list2.add(storage);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }

    public RestResult getGroupAllStorage(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = storageRepository.total(group.getGid(), null);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        // 查询联系人
        val list2 = new ArrayList<HashMap<String, Object>>();
        val list = storageRepository.pagination(group.getGid(), 1, total, null);
        if (null != list && !list.isEmpty()) {
            for (TStorage s : list) {
                val storage = new HashMap<String, Object>();
                storage.put("id", s.getId());
                storage.put("area", String.valueOf(s.getArea()));
                storage.put("name", s.getName());
                storage.put("address", s.getAddress());
                storage.put("contact", s.getContact());
                storage.put("phone", s.getPhone());
                list2.add(storage);
            }
        }
        return RestResult.ok(new PageData(total, list2));
    }
}
