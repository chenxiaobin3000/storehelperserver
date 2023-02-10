package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketCommodityDetailMapper;
import com.cxb.storehelperserver.model.TMarketCommodityDetail;
import com.cxb.storehelperserver.model.TMarketCommodityDetailExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 市场商品销售明细仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketCommodityDetailRepository extends BaseRepository<TMarketCommodityDetail> {
    @Resource
    private TMarketCommodityDetailMapper marketCommodityDetailMapper;

    public MarketCommodityDetailRepository() {
        init("marketCommDetail::");
    }

    public TMarketCommodityDetail find(int id) {
        TMarketCommodityDetail detail = getCache(id, TMarketCommodityDetail.class);
        if (null != detail) {
            return detail;
        }

        // 缓存没有就查询数据库
        detail = marketCommodityDetailMapper.selectByPrimaryKey(id);
        if (null != detail) {
            setCache(id, detail);
        }
        return detail;
    }

    public int total(int gid, int mid, String search) {
        TMarketCommodityDetailExample example = new TMarketCommodityDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        return (int) marketCommodityDetailMapper.countByExample(example);
    }

    public List<TMarketCommodityDetail> pagination(int gid, int page, int limit, int mid, String search) {
        TMarketCommodityDetailExample example = new TMarketCommodityDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return marketCommodityDetailMapper.selectByExample(example);
    }

    public boolean insert(TMarketCommodityDetail row) {
        if (marketCommodityDetailMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TMarketCommodityDetail row) {
        if (marketCommodityDetailMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return marketCommodityDetailMapper.deleteByPrimaryKey(id) > 0;
    }
}
