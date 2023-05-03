package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketCommodityMapper;
import com.cxb.storehelperserver.model.TMarketCommodity;
import com.cxb.storehelperserver.model.TMarketCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyMarketCommodityMapper;
import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
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

    public TMarketCommodity find(int aid, int cid) {
        TMarketCommodity marketCommodity = getCache(joinKey(aid, cid), TMarketCommodity.class);
        if (null != marketCommodity) {
            return marketCommodity;
        }

        // 缓存没有就查询数据库
        TMarketCommodityExample example = new TMarketCommodityExample();
        example.or().andAidEqualTo(aid).andCidEqualTo(cid);
        marketCommodity = marketCommodityMapper.selectOneByExample(example);
        if (null != marketCommodity) {
            setCache(joinKey(aid, cid), marketCommodity);
        }
        return marketCommodity;
    }

    public List<TMarketCommodity> findByCid(int cid) {
        TMarketCommodityExample example = new TMarketCommodityExample();
        example.or().andCidEqualTo(cid);
        return marketCommodityMapper.selectByExample(example);
    }

    public List<TMarketCommodity> findAll(int aid) {
        TMarketCommodityExample example = new TMarketCommodityExample();
        example.or().andAidEqualTo(aid);
        return marketCommodityMapper.selectByExample(example);
    }

    public int total(int aid, String search) {
        if (null != search) {
            return myMarketCommodityMapper.count(aid, "%" + search + "%");
        } else {
            TMarketCommodityExample example = new TMarketCommodityExample();
            example.or().andAidEqualTo(aid);
            return (int) marketCommodityMapper.countByExample(example);
        }
    }

    public List<MyMarketCommodity> pagination(int aid, int page, int limit, String search) {
        if (null != search) {
            return myMarketCommodityMapper.pagination((page - 1) * limit, limit, aid, "%" + search + "%");
        } else {
            return myMarketCommodityMapper.pagination((page - 1) * limit, limit, aid, null);
        }
    }

    public boolean update(TMarketCommodity row) {
        delete(row.getAid(), row.getCid());
        if (marketCommodityMapper.insert(row) > 0) {
            setCache(joinKey(row.getAid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int aid, int cid) {
        TMarketCommodity marketCommodity = find(aid, cid);
        if (null == marketCommodity) {
            return false;
        }
        delCache(joinKey(aid, cid));
        return marketCommodityMapper.deleteByPrimaryKey(marketCommodity.getId()) > 0;
    }
}
