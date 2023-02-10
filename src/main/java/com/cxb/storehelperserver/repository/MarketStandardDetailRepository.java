package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketStandardDetailMapper;
import com.cxb.storehelperserver.model.TMarketStandardDetail;
import com.cxb.storehelperserver.model.TMarketStandardDetailExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 市场标品销售明细仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketStandardDetailRepository extends BaseRepository<TMarketStandardDetail> {
    @Resource
    private TMarketStandardDetailMapper marketStandardDetailMapper;

    public MarketStandardDetailRepository() {
        init("marketStanDetail::");
    }

    public TMarketStandardDetail find(int id) {
        TMarketStandardDetail detail = getCache(id, TMarketStandardDetail.class);
        if (null != detail) {
            return detail;
        }

        // 缓存没有就查询数据库
        detail = marketStandardDetailMapper.selectByPrimaryKey(id);
        if (null != detail) {
            setCache(id, detail);
        }
        return detail;
    }

    public int total(int gid, int mid, String search) {
        TMarketStandardDetailExample example = new TMarketStandardDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        return (int) marketStandardDetailMapper.countByExample(example);
    }

    public List<TMarketStandardDetail> pagination(int gid, int page, int limit, int mid, String search) {
        TMarketStandardDetailExample example = new TMarketStandardDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return marketStandardDetailMapper.selectByExample(example);
    }

    public boolean insert(TMarketStandardDetail row) {
        if (marketStandardDetailMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TMarketStandardDetail row) {
        if (marketStandardDetailMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return marketStandardDetailMapper.deleteByPrimaryKey(id) > 0;
    }
}
