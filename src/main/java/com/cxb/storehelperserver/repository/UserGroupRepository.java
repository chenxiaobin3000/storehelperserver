package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserGroupMapper;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.model.TUserGroupExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 用户公司仓库
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Repository
public class UserGroupRepository extends BaseRepository<TUserGroup> {
    @Resource
    private TUserGroupMapper userGroupMapper;

    public UserGroupRepository() {
        init("userGroup::");
    }

    public TUserGroup find(int uid) {
        TUserGroup userGroup = getCache(uid, TUserGroup.class);
        if (null != userGroup) {
            return userGroup;
        }

        // 缓存没有就查询数据库
        TUserGroupExample example = new TUserGroupExample();
        example.or().andUidEqualTo(uid);
        userGroup = userGroupMapper.selectOneByExample(example);
        if (null != userGroup) {
            setCache(uid, userGroup);
        }
        return userGroup;
    }

    /*
     * desc: 判断公司是否存在员工
     */
    public boolean checkUser(int gid) {
        TUserGroupExample example = new TUserGroupExample();
        example.or().andGidEqualTo(gid);
        return null != userGroupMapper.selectOneByExample(example);
    }

    public boolean insert(TUserGroup row) {
        if (userGroupMapper.insert(row) > 0) {
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TUserGroup row) {
        if (userGroupMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int uid) {
        delCache(uid);
        TUserGroupExample example = new TUserGroupExample();
        example.or().andUidEqualTo(uid);
        return userGroupMapper.deleteByExample(example) > 0;
    }
}
