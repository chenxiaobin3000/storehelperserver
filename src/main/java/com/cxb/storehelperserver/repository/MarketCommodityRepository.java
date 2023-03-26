package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketCommodityMapper;
import com.cxb.storehelperserver.model.TMarketCommodity;
import com.cxb.storehelperserver.model.TMarketCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyMarketCommodityMapper;
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

    @Resource
    private MyMarketCommodityMapper myMarketCommodityMapper;

    public MarketCommodityRepository() {
        init("marketComm::");
    }

    public TMarketCommodity find(int sid, int mid, int cid) {
        TMarketCommodity marketCommodity = getCache(joinKey(sid, mid, cid), TMarketCommodity.class);
        if (null != marketCommodity) {
            return marketCommodity;
        }

        // 缓存没有就查询数据库
        TMarketCommodityExample example = new TMarketCommodityExample();
        example.or().andSidEqualTo(sid).andMidEqualTo(mid).andCidEqualTo(cid);
        marketCommodity = marketCommodityMapper.selectOneByExample(example);
        if (null != marketCommodity) {
            setCache(joinKey(sid, mid, cid), marketCommodity);
        }
        return marketCommodity;
    }

    public int total(int sid, int mid, String search) {
        if (null != search) {
            return myMarketCommodityMapper.count(sid, mid, "%" + search + "%");
        } else {
            return myMarketCommodityMapper.count(sid, mid, null);
        }
    }


    public List<TMarketCommodity> pagination(int sid, int mid, int page, int limit, String search) {
        if (null != search) {
            return myMarketCommodityMapper.pagination((page - 1) * limit, limit, sid, mid, "%" + search + "%");
        } else {
            return myMarketCommodityMapper.pagination((page - 1) * limit, limit, sid, mid, null);
        }
    }

    public boolean update(TMarketCommodity row) {
        delete(row.getSid(), row.getMid(), row.getCid());
        if (marketCommodityMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getMid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int mid, int cid) {
        TMarketCommodity marketCommodity = find(sid, mid, cid);
        if (null == marketCommodity) {
            return false;
        }
        delCache(joinKey(sid, mid, cid));
        return marketCommodityMapper.deleteByPrimaryKey(marketCommodity.getId()) > 0;
    }
}
