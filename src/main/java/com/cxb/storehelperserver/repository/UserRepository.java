package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserMapper;
import com.cxb.storehelperserver.model.TUser;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 *
 */
@Repository
public class UserRepository extends BaseRepository {
    // logger

    @Resource
    private TUserMapper tUserMapper;

    /**
     *
     * @return
     */
    public String getUserName() {
        TUser user =  tUserMapper.selectByPrimaryKey(1);
        return user.getAccount();
    }
}
