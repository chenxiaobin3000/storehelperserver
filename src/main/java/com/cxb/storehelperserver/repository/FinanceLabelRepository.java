package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TFinanceLabelMapper;
import com.cxb.storehelperserver.model.TFinanceLabel;
import com.cxb.storehelperserver.model.TFinanceLabelExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 财务类目仓库
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Repository
public class FinanceLabelRepository extends BaseRepository<TFinanceLabel> {
    @Resource
    private TFinanceLabelMapper financeLabelMapper;

    private final String cacheGroupName;

    public FinanceLabelRepository() {
        init("finanLabel::");
        cacheGroupName = cacheName + "group::";
    }

    public TFinanceLabel find(int id) {
        TFinanceLabel financeLabel = getCache(id, TFinanceLabel.class);
        if (null != financeLabel) {
            return financeLabel;
        }

        // 缓存没有就查询数据库
        financeLabel = financeLabelMapper.selectByPrimaryKey(id);
        if (null != financeLabel) {
            setCache(id, financeLabel);
        }
        return financeLabel;
    }

    public List<TFinanceLabel> findByGroup(int gid) {
        List<TFinanceLabel> categories = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != categories) {
            return categories;
        }
        TFinanceLabelExample example = new TFinanceLabelExample();
        example.or().andGidEqualTo(gid);
        example.setOrderByClause("level asc");
        categories = financeLabelMapper.selectByExample(example);
        if (null != categories) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, categories);
        }
        return categories;
    }

    /*
     * desc: 判断公司是否存在财务类目
     */
    public boolean check(int gid, String name, int id) {
        TFinanceLabelExample example = new TFinanceLabelExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != financeLabelMapper.selectOneByExample(example);
        } else {
            TFinanceLabel financeLabel = financeLabelMapper.selectOneByExample(example);
            return null != financeLabel && !financeLabel.getId().equals(id);
        }
    }

    public boolean insert(TFinanceLabel row) {
        if (financeLabelMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TFinanceLabel row) {
        if (financeLabelMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TFinanceLabel financeLabel = find(id);
        if (null == financeLabel) {
            return false;
        }
        delCache(cacheGroupName + financeLabel.getGid());
        delCache(id);
        return financeLabelMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean checkChildren(int id) {
        TFinanceLabelExample example = new TFinanceLabelExample();
        example.or().andParentEqualTo(id);
        TFinanceLabel financeLabel = financeLabelMapper.selectOneByExample(example);
        return null != financeLabel;
    }
}
