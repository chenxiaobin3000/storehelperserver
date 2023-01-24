package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserRoleMpMapper;
import com.cxb.storehelperserver.model.TOrderReviewer;
import com.cxb.storehelperserver.model.TUserRoleMp;
import com.cxb.storehelperserver.model.TUserRoleMpExample;
import com.cxb.storehelperserver.repository.mapper.MyUserRoleMpMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

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

    @Resource
    private MyUserRoleMpMapper myUserRoleMpMapper;

    public UserRoleMpRepository() {
        init("userRoleMp::");
    }

    public TUserRoleMp find(int uid) {
        TUserRoleMp userRoleMp = getCache(uid, TUserRoleMp.class);
        if (null != userRoleMp) {
            return userRoleMp;
        }

        // 缓存没有就查询数据库
        TUserRoleMpExample example = new TUserRoleMpExample();
        example.or().andUidEqualTo(uid);
        userRoleMp = userRoleMpMapper.selectOneByExample(example);
        if (null != userRoleMp) {
            setCache(uid, userRoleMp);
        }
        return userRoleMp;
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

    public List<TOrderReviewer> getUserRoleMpPerms(int gid, int p1, int p2, int p3) {
        return myUserRoleMpMapper.select(gid, p1, p2, p3);
    }
}
