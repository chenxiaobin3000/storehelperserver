package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TConfigPermissionMapper;
import com.cxb.storehelperserver.model.TConfigPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 权限配置仓库
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Repository
public class ConfigPermissionRepository extends BaseRepository<String> {
    @Resource
    private TConfigPermissionMapper configPermissionMapper;

    public ConfigPermissionRepository() {
        init("cfgPermission::");
    }

    public String find(int id) {
        String role = getCache(id, String.class);
        if (null != role) {
            return role;
        }

        // 缓存没有就查询数据库
        TConfigPermission permission = configPermissionMapper.selectByPrimaryKey(id);
        if (null != permission) {
            role = permission.getName();
            setCache(id, role);
        }
        return role;
    }
}
