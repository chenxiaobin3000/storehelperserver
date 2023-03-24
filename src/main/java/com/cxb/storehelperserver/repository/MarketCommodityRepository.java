package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketCommodityMapper;
import com.cxb.storehelperserver.model.TMarketCommodity;
import com.cxb.storehelperserver.model.TMarketCommodityExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

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

    public boolean update(TMarketCommodity row) {
        delete(row.getGid(), row.getMid(), row.getCid());
        if (marketCommodityMapper.insert(row) > 0) {
            setCache(joinKey(row.getGid(), row.getMid(), row.getCid()), row);
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
        return marketCommodityMapper.deleteByPrimaryKey(marketCommodity.getId()) > 0;
    }
}
