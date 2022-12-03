package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserMapper;
import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.model.TUserExample;
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
        TUser user = getCache(String.valueOf(id), TUser.class);
        if (null != user) {
            return user;
        }
        return userMapper.selectByPrimaryKey(id);
    }

    public boolean insert(TUser row) {
        int ret = userMapper.insert(row);
        if (ret > 0) {
            setCache(String.valueOf(ret), row);
            return true;
        }
        return false;
    }

    public boolean update(TUser row) {
        int ret = userMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(String.valueOf(row.getId()), row);
            return true;
        }
        return false;
    }
}
