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
        if (null != tGroup && null == tGroup.getDtime()) { // 软删除
            setCache(id, tGroup);
            return tGroup;
        }
        return null;
    }

    public int total(String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TGroupExample example = new TGroupExample();
            example.or().andDtimeIsNull().andNameLike("%" + search + "%"); // 软删除
            return (int) groupMapper.countByExample(example);
        } else {
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
    }

    public List<TGroup> pagination(int page, int limit, String search) {
        TGroupExample example = new TGroupExample();
        if (null == search || search.isEmpty()) {
            example.or().andDtimeIsNull(); // 软删除
        } else {
            example.or().andDtimeIsNull().andNameLike("%" + search + "%"); // 软删除
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return groupMapper.selectByExample(example);
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

    public boolean delete(int id) {
        delCache(id);
        delTotalCache(0);
        TGroup group = find(id);
        if (null != group && null == group.getDtime()) {
            group.setDtime(new Date()); // 软删除
            return groupMapper.updateByPrimaryKey(group) > 0;
        }
        return true;
    }
}
