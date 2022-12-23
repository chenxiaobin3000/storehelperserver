package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TConfigPermissionMapper;
import com.cxb.storehelperserver.model.TConfigPermission;
import com.cxb.storehelperserver.model.TConfigPermissionExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

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

    public String all() {
        String permissions = getCache(0, String.class);
        if (null != permissions) {
            return permissions;
        }

        // 缓存没有就查询数据库
        permissions = "";
        StringBuilder sb = new StringBuilder();
        TConfigPermissionExample example = new TConfigPermissionExample();
        List<TConfigPermission> list = configPermissionMapper.selectByExample(example);
        if (null != list) {
            for (TConfigPermission cp : list) {
                sb.append(cp.getId());
                sb.append(',');
            }
            permissions = sb.toString();
            if (permissions.length() > 0) {
                permissions = permissions.substring(0, permissions.length() - 1);
            }
            setCache(0, permissions);
        }
        return permissions;
    }
}
