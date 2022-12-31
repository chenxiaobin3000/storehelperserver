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

    private final String cachePhone;

    public UserRepository() {
        init("user::");
        cachePhone = cacheName + "p::";
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
        return null;
    }

    public TUser findByPhone(String phone) {
        TUser user = getCache(cachePhone + phone, TUser.class);
        if (null != user) {
            return user;
        }
        TUserExample example = new TUserExample();
        example.or().andPhoneEqualTo(phone);
        user = userMapper.selectOneByExample(example);
        if (null != user) {
            setCache(cachePhone + phone, user);
            return user;
        }
        return null;
    }

    public boolean insert(TUser row) {
        if (userMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TUser row) {
        // 检测手机号是否变动
        TUser user = find(row.getId());
        if (null == user) {
            return false;
        }
        if (!user.getPhone().equals(row.getPhone())) {
            delCache(cachePhone + user.getPhone());
        }

        if (userMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            setCache(cachePhone + row.getPhone(), user);
            return true;
        }
        return false;
    }
}
