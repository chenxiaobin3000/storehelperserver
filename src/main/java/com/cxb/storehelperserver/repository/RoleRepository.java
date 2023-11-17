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
        TRole role = getCache(id, TRole.class);
        if (null != role) {
            return role;
        }

        // 缓存没有就查询数据库
        role = roleMapper.selectByPrimaryKey(id);
        if (null != role) {
            setCache(id, role);
        }
        return role;
    }

    public List<TRole> all(String search) {
        List<TRole> roles = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName));
        if (null != roles) {
            return roles;
        }
        TRoleExample example = new TRoleExample();
        if (null == search || search.isEmpty()) {
            roles = roleMapper.selectByExample(example);
            if (null != roles) {
                redisTemplate.opsForValue().set(cacheName + cacheGroupName, roles);
            }
        } else {
            example.or().andNameLike("%" + search + "%");
            roles = roleMapper.selectByExample(example);
        }
        return roles;
    }

    /*
     * desc: 判断公司是否存在角色
     */
    public boolean check(String name, int id) {
        TRoleExample example = new TRoleExample();
        example.or().andNameEqualTo(name);
        if (0 == id) {
            return null != roleMapper.selectOneByExample(example);
        } else {
            TRole role = roleMapper.selectOneByExample(example);
            return null != role && !role.getId().equals(id);
        }
    }

    public boolean insert(TRole row) {
        if (roleMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName);
            return true;
        }
        return false;
    }

    public boolean update(TRole row) {
        if (roleMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TRole role = find(id);
        if (null == role) {
            return false;
        }
        delCache(cacheGroupName);
        delCache(id);
        return roleMapper.deleteByPrimaryKey(id) > 0;
    }
}
