package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TRolePermissionMpMapper;
import com.cxb.storehelperserver.model.TRolePermissionMp;
import com.cxb.storehelperserver.model.TRolePermissionMpExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 小程序角色权限仓库
 * auth: cxb
 * date: 2023/1/17
 */
@Slf4j
@Repository
public class RolePermissionMpRepository extends BaseRepository<List> {
    @Resource
    private TRolePermissionMpMapper rolePermissionMpMapper;

    public RolePermissionMpRepository() {
        init("rolePermMp::");
    }

    public List<Integer> find(int rid) {
        List<Integer> rets = getCache(rid, List.class);
        if (null != rets) {
            return rets;
        }

        // 缓存没有就查询数据库
        rets = new ArrayList<>();
        TRolePermissionMpExample example = new TRolePermissionMpExample();
        example.or().andRidEqualTo(rid);
        List<TRolePermissionMp> rolePermissionMps = rolePermissionMpMapper.selectByExample(example);
        if (null != rolePermissionMps && !rolePermissionMps.isEmpty()) {
            for (TRolePermissionMp role : rolePermissionMps) {
                rets.add(role.getPid());
            }
            setCache(rid, rets);
        }
        return rets;
    }

    public boolean insert(int rid, List<Integer> permissions) {
        List<Integer> rets = new ArrayList<>();
        TRolePermissionMp rolePermissionMp = new TRolePermissionMp();
        rolePermissionMp.setRid(rid);
        for (Integer p : permissions) {
            rolePermissionMp.setId(null);
            rolePermissionMp.setPid(p);
            if (rolePermissionMpMapper.insert(rolePermissionMp) < 1) {
                return false;
            }
            rets.add(p);
        }
        setCache(rid, rets);
        return true;
    }

    public boolean update(int rid, List<Integer> permissions) {
        if (!delete(rid)) {
            return false;
        }
        return insert(rid, permissions);
    }

    public boolean delete(int rid) {
        delCache(rid);
        TRolePermissionMpExample example = new TRolePermissionMpExample();
        example.or().andRidEqualTo(rid);
        return rolePermissionMpMapper.deleteByExample(example) > 0;
    }
}
