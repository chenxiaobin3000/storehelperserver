package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TRoleMapper;
import com.cxb.storehelperserver.model.TRole;
import com.cxb.storehelperserver.model.TRoleExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

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

    private final String cacheGroupName;

    public RoleRepository() {
        init("role::");
        cacheGroupName = cacheName + "group::";
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

    public List<TRole> findByGroup(int gid) {
        List<TRole> roles = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != roles) {
            return roles;
        }
        TRoleExample example = new TRoleExample();
        example.or().andGidEqualTo(gid);
        roles = roleMapper.selectByExample(example);
        if (null != roles) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, roles);
        }
        return roles;
    }

    public List<TRole> all(int gid, String search) {
        TRoleExample example = new TRoleExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        return roleMapper.selectByExample(example);
    }

    public boolean insert(TRole row) {
        if (roleMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheName + cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TRole row) {
        if (roleMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheName + cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TRole role = find(id);
        if (null == role) {
            return false;
        }
        delCache(cacheName + cacheGroupName + role.getGid());

        delCache(id);
        if (roleMapper.deleteByPrimaryKey(id) <= 0) {
            return false;
        }
        return true;
    }
}
