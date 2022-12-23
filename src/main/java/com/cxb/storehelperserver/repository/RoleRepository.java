package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TRoleMapper;
import com.cxb.storehelperserver.model.TRole;
import com.cxb.storehelperserver.model.TRoleExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 角色仓库
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Repository
public class RoleRepository extends BaseRepository<TRole> {
    @Resource
    private TRoleMapper roleMapper;

    public RoleRepository() {
        init("role::");
    }

    public TRole find(int id) {
        TRole tRole = getCache(id, TRole.class);
        if (null != tRole) {
            return tRole;
        }

        // 缓存没有就查询数据库
        tRole = roleMapper.selectByPrimaryKey(id);
        if (null != tRole) {
            setCache(id, tRole);
        }
        return tRole;
    }

    public boolean insert(TRole row) {
        int ret = roleMapper.insert(row);
        if (ret > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TRole row) {
        int ret = roleMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        int ret = roleMapper.deleteByPrimaryKey(id);
        if (ret <= 0) {
            return false;
        }
        delCache(id);
        return true;
    }
}
