package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketCommodityMapper;
import com.cxb.storehelperserver.model.TMarketCommodity;
import com.cxb.storehelperserver.model.TMarketCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyMarketCommodityMapper;
import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
import com.cxb.storehelperserver.repository.model.MyMarketDetailCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
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

    @Resource
    private MyMarketCommodityMapper myMarketCommodityMapper;

    public MarketCommodityRepository() {
        init("marketComm::");
    }

    public TMarketCommodity find(int gid, int mid, int cid) {
        TMarketCommodity marketCommodity = getCache(joinKey(gid, mid, cid), TMarketCommodity.class);
        if (null != marketCommodity) {
            return marketCommodity;
        }

        // 缓存没有就查询数据库
        TMarketCommodityExample example = new TMarketCommodityExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid).andCidEqualTo(cid);
        marketCommodity = marketCommodityMapper.selectOneByExample(example);
        if (null != marketCommodity) {
            setCache(joinKey(gid, mid, cid), marketCommodity);
        }
        return marketCommodity;
    }

    public int total(int gid, int mid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TMarketCommodityExample example = new TMarketCommodityExample();
            example.or().andGidEqualTo(gid).andMidEqualTo(mid).andNameLike("%" + search + "%");
            return (int) marketCommodityMapper.countByExample(example);
        } else {
            int total = getTotalCache(joinKey(gid, mid));
            if (0 != total) {
                return total;
            }
            TMarketCommodityExample example = new TMarketCommodityExample();
            example.or().andGidEqualTo(gid).andMidEqualTo(mid);
            total = (int) marketCommodityMapper.countByExample(example);
            setTotalCache(joinKey(gid, mid), total);
            return total;
        }
    }

    public List<MyMarketCommodity> pagination(int gid, int page, int limit, int mid, String search) {
        if (null != search) {
            return myMarketCommodityMapper.select((page - 1) * limit, limit, gid, mid, "%" + search + "%");
        } else {
            return myMarketCommodityMapper.select((page - 1) * limit, limit, gid, mid, null);
        }
    }

    public List<MyMarketDetailCommodity> paginationDetail(int gid, int page, int limit, int mid, Date date, String search) {
        if (null != search) {
            return myMarketCommodityMapper.selectDetail((page - 1) * limit, limit, gid, mid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myMarketCommodityMapper.selectDetail((page - 1) * limit, limit, gid, mid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean update(TMarketCommodity row) {
        delete(row.getGid(), row.getMid(), row.getCid());
        if (marketCommodityMapper.insert(row) > 0) {
            setCache(joinKey(row.getGid(), row.getMid(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getMid()));
            return true;
        }
        return false;
    }

    public boolean delete(int gid, int mid, int cid) {
        TMarketCommodity marketCommodity = find(gid, mid, cid);
        if (null == marketCommodity) {
            return false;
        }
        delCache(joinKey(gid, mid, cid));
        delTotalCache(joinKey(gid, mid));
        return marketCommodityMapper.deleteByPrimaryKey(marketCommodity.getId()) > 0;
    }
}
