package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserRoleMapper;
import com.cxb.storehelperserver.model.TUserRole;
import com.cxb.storehelperserver.model.TUserRoleExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 用户角色仓库
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Repository
public class UserRoleRepository extends BaseRepository<TUserRole> {
    @Resource
    private TUserRoleMapper userRoleMapper;

    public UserRoleRepository() {
        init("userRole::");
    }

    public TUserRole find(int uid) {
        TUserRole tUserRole = getCache(uid, TUserRole.class);
        if (null != tUserRole) {
            return tUserRole;
        }

        // 缓存没有就查询数据库
        TUserRoleExample example = new TUserRoleExample();
        example.or().andUidEqualTo(uid);
        tUserRole = userRoleMapper.selectOneByExample(example);
        if (null != tUserRole) {
            setCache(uid, tUserRole);
        }
        return tUserRole;
    }

    public boolean insert(TUserRole row) {
        if (userRoleMapper.insert(row) > 0) {
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TUserRole row) {
        if (userRoleMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(TUserRole row) {
        if (userRoleMapper.deleteByPrimaryKey(row.getId()) <= 0) {
            return false;
        }
        delCache(row.getUid());
        return true;
    }
}
