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
        init("rolePerm::");
    }

    public List<Integer> find(int rid) {
        List<Integer> rets = getCache(rid, List.class);
        if (null != rets) {
            return rets;
        }

        // 缓存没有就查询数据库
        rets = new ArrayList<>();
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

    public boolean insert(int rid, List<Integer> permissions) {
        List<Integer> rets = new ArrayList<>();
        TRolePermission rolePermission = new TRolePermission();
        rolePermission.setRid(rid);
        for (Integer p : permissions) {
            rolePermission.setId(null);
            rolePermission.setPid(p);
            if (rolePermissionMapper.insert(rolePermission) < 1) {
                return false;
            }
            rets.add(p);
        }
        setCache(rid, rets);
        return true;
    }

    public boolean update(int rid, List<Integer> permissions) {
        delete(rid);
        return insert(rid, permissions);
    }

    public boolean delete(int rid) {
        delCache(rid);
        TRolePermissionExample example = new TRolePermissionExample();
        example.or().andRidEqualTo(rid);
        return rolePermissionMapper.deleteByExample(example) > 0;
    }
}
