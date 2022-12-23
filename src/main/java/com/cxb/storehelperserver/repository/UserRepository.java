package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserMapper;
import com.cxb.storehelperserver.model.TUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 用户仓库
 * auth: cxb
 * date: 2022/11/29
 */
@Slf4j
@Repository
public class UserRepository extends BaseRepository<TUser> {
    @Resource
    private TUserMapper userMapper;

    public UserRepository() {
        init("user::");
    }

    public TUser find(int id) {
        TUser user = getCache(id, TUser.class);
        if (null != user) {
            return user;
        }
        user = userMapper.selectByPrimaryKey(id);
        if (null != user) {
            setCache(id, user);
            return user;
        }
        return user;
    }

    public boolean insert(TUser row) {
        int ret = userMapper.insert(row);
        if (ret > 0) {
            setCache(ret, row);
            return true;
        }
        return false;
    }

    public boolean update(TUser row) {
        int ret = userMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }
}
