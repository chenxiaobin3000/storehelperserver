package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TRoleMapper;
import com.cxb.storehelperserver.model.TRole;
import com.cxb.storehelperserver.model.TRoleExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

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

    public List<TRole> all(int gid) {
        TRoleExample example = new TRoleExample();
        example.or().andGidEqualTo(gid);
        return roleMapper.selectByExample(example);
    }

    public boolean insert(TRole row) {
        if (roleMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TRole row) {
        if (roleMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        if (roleMapper.deleteByPrimaryKey(id) <= 0) {
            return false;
        }
        delCache(id);
        return true;
    }
}
