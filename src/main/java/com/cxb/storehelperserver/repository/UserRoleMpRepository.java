package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserRoleMpMapper;
import com.cxb.storehelperserver.model.TUserRoleMp;
import com.cxb.storehelperserver.model.TUserRoleMpExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 用户小程序角色仓库
 * auth: cxb
 * date: 2023/1/17
 */
@Slf4j
@Repository
public class UserRoleMpRepository extends BaseRepository<TUserRoleMp> {
    @Resource
    private TUserRoleMpMapper userRoleMpMapper;

    public UserRoleMpRepository() {
        init("userRoleMp::");
    }

    public TUserRoleMp find(int uid) {
        TUserRoleMp tUserRoleMp = getCache(uid, TUserRoleMp.class);
        if (null != tUserRoleMp) {
            return tUserRoleMp;
        }

        // 缓存没有就查询数据库
        TUserRoleMpExample example = new TUserRoleMpExample();
        example.or().andUidEqualTo(uid);
        tUserRoleMp = userRoleMpMapper.selectOneByExample(example);
        if (null != tUserRoleMp) {
            setCache(uid, tUserRoleMp);
        }
        return tUserRoleMp;
    }

    public boolean insert(TUserRoleMp row) {
        if (userRoleMpMapper.insert(row) > 0) {
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TUserRoleMp row) {
        if (userRoleMpMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(TUserRoleMp row) {
        delCache(row.getUid());
        return userRoleMpMapper.deleteByPrimaryKey(row.getId()) > 0;
    }
}
