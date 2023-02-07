package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketDetailMapper;
import com.cxb.storehelperserver.model.TMarketDetail;
import com.cxb.storehelperserver.model.TMarketDetailExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 市场销售明细仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketDetailRepository extends BaseRepository<TMarketDetail> {
    @Resource
    private TMarketDetailMapper marketDetailMapper;

    public MarketDetailRepository() {
        init("marketDetail::");
    }

    public TMarketDetail find(int id) {
        TMarketDetail marketDetail = getCache(id, TMarketDetail.class);
        if (null != marketDetail) {
            return marketDetail;
        }

        // 缓存没有就查询数据库
        marketDetail = marketDetailMapper.selectByPrimaryKey(id);
        if (null != marketDetail) {
            setCache(id, marketDetail);
        }
        return marketDetail;
    }

    public int total(int gid) {
        int total = getTotalCache(gid);
        if (0 != total) {
            return total;
        }
        TMarketDetailExample example = new TMarketDetailExample();
        example.or().andGidEqualTo(gid);
        total = (int) marketDetailMapper.countByExample(example);
        setTotalCache(gid, total);
        return total;
    }

    public List<TMarketDetail> pagination(int gid, int page, int limit) {
        TMarketDetailExample example = new TMarketDetailExample();
        example.or().andGidEqualTo(gid);
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return marketDetailMapper.selectByExample(example);
    }

    public boolean insert(TMarketDetail row) {
        if (marketDetailMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TMarketDetail row) {
        if (marketDetailMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TMarketDetail marketDetail = find(id);
        if (null == marketDetail) {
            return false;
        }
        delCache(id);
        delTotalCache(marketDetail.getGid());
        return marketDetailMapper.deleteByPrimaryKey(id) > 0;
    }
}
