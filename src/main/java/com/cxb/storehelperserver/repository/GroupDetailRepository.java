package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TGroupDetailMapper;
import com.cxb.storehelperserver.model.TGroupDetail;
import com.cxb.storehelperserver.model.TGroupDetailExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 公司资金明细仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class GroupDetailRepository extends BaseRepository<TGroupDetail> {
    @Resource
    private TGroupDetailMapper groupDetailMapper;

    public GroupDetailRepository() {
        init("groupDetail::");
    }

    public TGroupDetail find(int id) {
        TGroupDetail groupDetail = getCache(id, TGroupDetail.class);
        if (null != groupDetail) {
            return groupDetail;
        }

        // 缓存没有就查询数据库
        groupDetail = groupDetailMapper.selectByPrimaryKey(id);
        if (null != groupDetail) {
            setCache(id, groupDetail);
        }
        return groupDetail;
    }

    public int total(int gid) {
        int total = getTotalCache(gid);
        if (0 != total) {
            return total;
        }
        TGroupDetailExample example = new TGroupDetailExample();
        example.or().andGidEqualTo(gid);
        total = (int) groupDetailMapper.countByExample(example);
        setTotalCache(gid, total);
        return total;
    }

    public List<TGroupDetail> pagination(int gid, int page, int limit) {
        TGroupDetailExample example = new TGroupDetailExample();
        example.or().andGidEqualTo(gid);
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return groupDetailMapper.selectByExample(example);
    }

    public boolean insert(TGroupDetail row) {
        if (groupDetailMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TGroupDetail row) {
        if (groupDetailMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TGroupDetail groupDetail = find(id);
        if (null == groupDetail) {
            return false;
        }
        delCache(id);
        delTotalCache(groupDetail.getGid());
        return groupDetailMapper.deleteByPrimaryKey(id) > 0;
    }
}