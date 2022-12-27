package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TGroupMapper;
import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TGroupExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 公司仓库
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Repository
public class GroupRepository extends BaseRepository<TGroup> {
    @Resource
    private TGroupMapper groupMapper;

    private String cacheUserName;

    public GroupRepository() {
        init("group::");
        cacheUserName = cacheName + "user::";
    }

    public TGroup find(int id) {
        TGroup tGroup = getCache(id, TGroup.class);
        if (null != tGroup) {
            return tGroup;
        }

        // 缓存没有就查询数据库
        tGroup = groupMapper.selectByPrimaryKey(id);
        if (null != tGroup) {
            setCache(id, tGroup);
        }
        return tGroup;
    }

    public boolean insert(TGroup row) {
        if (groupMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TGroup row) {
        if (groupMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public void updateByUid(int id, int gid) {
        TGroup tGroup = getCache(cacheUserName + id, TGroup.class);
        if (null != tGroup) {
            tGroup.setId(gid);
            setCache(cacheUserName + id, tGroup);
        }
    }

    public boolean delete(int id) {
        if (groupMapper.deleteByPrimaryKey(id) <= 0) {
            return false;
        }
        delCache(id);
        return true;
    }
}
