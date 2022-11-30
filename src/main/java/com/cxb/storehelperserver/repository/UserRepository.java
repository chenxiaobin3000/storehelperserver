package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserMapper;
import com.cxb.storehelperserver.model.TUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 用户仓库
 * auth: cxb
 * date: 2022/11/29
 */
@Repository
public class UserRepository extends BaseRepository<TUser> {
    private static final Logger logger = LogManager.getLogger(UserRepository.class);

    @Resource
    private TUserMapper tUserMapper;

    public UserRepository() {
        init("user::");
    }

    public TUser find(int id) {
        TUser user = getCache(String.valueOf(id), TUser.class);
        if (null != user) {
            return user;
        }
        return tUserMapper.selectByPrimaryKey(id);
    }

    public int insert(TUser row) {
        int ret = tUserMapper.insert(row);
        setCache(String.valueOf(ret), row);
        return ret;
    }

    public int update(TUser row) {
        setCache(String.valueOf(row.getId()), row);
        return tUserMapper.updateByPrimaryKey(row);
    }
}
