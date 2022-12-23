package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TGroupMapper;
import com.cxb.storehelperserver.model.TGroup;
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

    public GroupRepository() {
        init("group::");
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
        int ret = groupMapper.insert(row);
        if (ret > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TGroup row) {
        int ret = groupMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        int ret = groupMapper.deleteByPrimaryKey(id);
        if (ret <= 0) {
            return false;
        }
        delCache(id);
        return true;
    }
}
