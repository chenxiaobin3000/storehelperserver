package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserMapper;
import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.model.TUserExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

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

    public int total(String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TUserExample example = new TUserExample();
            example.or().andNameLike("%" + search + "%");
            return (int) userMapper.countByExample(example);
        } else {
            int total = getTotalCache(0);
            if (0 != total) {
                return total;
            }
            TUserExample example = new TUserExample();
            total = (int) userMapper.countByExample(example);
            setTotalCache(0, total);
            return total;
        }
    }

    public List<TUser> pagination(int page, int limit, String search) {
        TUserExample example = new TUserExample();
        if (null != search) {
            example.or().andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return userMapper.selectByExample(example);
    }

    public TUser insert(String account, String phone) {
        TUser row = new TUser();
        row.setName(account);
        row.setPhone(phone);
        if (userMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(0);
            return row;
        }
        return null;
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

    public boolean delete(int id) {
        TUser user = find(id);
        if (null == user) {
            return false;
        }
        delCache(id);
        delCache(cachePhone + user.getPhone());
        return userMapper.deleteByPrimaryKey(id) > 0;
    }
}
