package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TRolePermissionMapper;
import com.cxb.storehelperserver.model.TRolePermission;
import com.cxb.storehelperserver.model.TRolePermissionExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

/**
 * desc: 角色权限仓库
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Repository
public class RolePermissionRepository extends BaseRepository<List> {
    @Resource
    private TRolePermissionMapper rolePermissionMapper;

    public RolePermissionRepository() {
        init("rolePermission::");
    }

    public List<Integer> find(int rid) {
        List<Integer> rets = getCache(rid, List.class);
        if (null != rets) {
            return rets;
        }

        // 缓存没有就查询数据库
        TRolePermissionExample example = new TRolePermissionExample();
        example.or().andRidEqualTo(rid);
        List<TRolePermission> rolePermissions = rolePermissionMapper.selectByExample(example);
        if (null != rolePermissions && !rolePermissions.isEmpty()) {
            for (TRolePermission role : rolePermissions) {
                rets.add(role.getPid());
            }
            setCache(rid, rets);
        }
        return rets;
    }

    public boolean insert(TRolePermission row) {
        int ret = rolePermissionMapper.insert(row);
        if (ret > 0) {
            int rid = row.getRid();
            List<Integer> rets = getCache(rid, List.class);
            if (null == rets) {
                rets = new ArrayList<>();
            }
            rets.add(row.getPid());
            setCache(rid, rets);
            return true;
        }
        return false;
    }

    public boolean delete(int rid) {
        TRolePermissionExample example = new TRolePermissionExample();
        example.or().andRidEqualTo(rid);
        int ret = rolePermissionMapper.deleteByExample(example);
        if (ret <= 0) {
            return false;
        }
        delCache(rid);
        return true;
    }
}
