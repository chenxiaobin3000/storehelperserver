package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TGroupMapper;
import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TGroupExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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

    private final String cacheUserName;

    public GroupRepository() {
        init("group::");
        cacheUserName = cacheName + "user::";
    }

    public int total() {
        int total = getTotalCache(0);
        if (0 != total) {
            return total;
        }
        TGroupExample example = new TGroupExample();
        example.or().andDtimeIsNull(); // 软删除
        total = (int) groupMapper.countByExample(example);
        setTotalCache(0, total);
        return total;
    }

    public List<TGroup> pagination(int page, int limit) {
        TGroupExample example = new TGroupExample();
        example.or().andDtimeIsNull(); // 软删除
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return groupMapper.selectByExample(example);
    }

    public TGroup find(int id) {
        TGroup tGroup = getCache(id, TGroup.class);
        if (null != tGroup) {
            return tGroup;
        }

        // 缓存没有就查询数据库
        tGroup = groupMapper.selectByPrimaryKey(id);
        if (null != tGroup && null == tGroup.getDtime()) { // 软删除
            setCache(id, tGroup);
            return tGroup;
        }
        return null;
    }

    public boolean insert(TGroup row) {
        if (groupMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(0);
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
        delCache(id);
        TGroup group = find(id);
        if (null != group && null == group.getDtime()) {
            group.setDtime(new Date()); // 软删除
            return groupMapper.updateByPrimaryKey(group) > 0;
        }
        return true;
    }
}
