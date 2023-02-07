package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketCommodityMapper;
import com.cxb.storehelperserver.model.TMarketCommodity;
import com.cxb.storehelperserver.model.TMarketCommodityExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 市场商品对接仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketCommodityRepository extends BaseRepository<TMarketCommodity> {
    @Resource
    private TMarketCommodityMapper marketCommodityMapper;

    public MarketCommodityRepository() {
        init("marketComm::");
    }

    public TMarketCommodity find(int id) {
        TMarketCommodity marketCommodity = getCache(id, TMarketCommodity.class);
        if (null != marketCommodity) {
            return marketCommodity;
        }

        // 缓存没有就查询数据库
        marketCommodity = marketCommodityMapper.selectByPrimaryKey(id);
        if (null != marketCommodity) {
            setCache(id, marketCommodity);
        }
        return marketCommodity;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TMarketCommodityExample example = new TMarketCommodityExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) marketCommodityMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TMarketCommodityExample example = new TMarketCommodityExample();
            example.or().andGidEqualTo(gid);
            total = (int) marketCommodityMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TMarketCommodity> pagination(int gid, int page, int limit, String search) {
        TMarketCommodityExample example = new TMarketCommodityExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return marketCommodityMapper.selectByExample(example);
    }

    public boolean insert(TMarketCommodity row) {
        if (marketCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TMarketCommodity row) {
        if (marketCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TMarketCommodity marketCommodity = find(id);
        if (null == marketCommodity) {
            return false;
        }
        delCache(id);
        delTotalCache(marketCommodity.getGid());
        return marketCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
