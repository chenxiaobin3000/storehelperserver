package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserMapper;
import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.model.TUserExample;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * desc:
     */
    public String getUserName() {
        TUserExample userExample = new TUserExample();
        userExample.or().andAccountEqualTo("test");
        TUser user = tUserMapper.selectByExample(userExample).get(0);
        redisTemplate.opsForValue().set("test", user);
        TUser user2 = (TUser)redisTemplate.opsForValue().get("test");
        return user.getPassword() + user2.getAccount();
    }
}
