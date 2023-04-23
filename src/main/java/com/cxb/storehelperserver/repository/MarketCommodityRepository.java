package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityStorageMapper;
import com.cxb.storehelperserver.mapper.TMarketCommodityMapper;
import com.cxb.storehelperserver.model.TCommodityStorageExample;
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
    private TCommodityStorageMapper commodityStorageMapper;

    @Resource
    private MyMarketCommodityMapper myMarketCommodityMapper;

    public MarketCommodityRepository() {
        init("marketComm::");
    }

    public TMarketCommodity find(int sid, int aid, int asid, int cid) {
        TMarketCommodity marketCommodity = getCache(joinKey(sid, aid, asid, cid), TMarketCommodity.class);
        if (null != marketCommodity) {
            return marketCommodity;
        }

        // 缓存没有就查询数据库
        TMarketCommodityExample example = new TMarketCommodityExample();
        example.or().andSidEqualTo(sid).andAidEqualTo(aid).andAsidEqualTo(asid).andCidEqualTo(cid);
        marketCommodity = marketCommodityMapper.selectOneByExample(example);
        if (null != marketCommodity) {
            setCache(joinKey(sid, aid, asid, cid), marketCommodity);
        }
        return marketCommodity;
    }

    public List<TMarketCommodity> findAll(int sid, int aid, int asid) {
        TMarketCommodityExample example = new TMarketCommodityExample();
        example.or().andSidEqualTo(sid).andAidEqualTo(aid).andAsidEqualTo(asid);
        return marketCommodityMapper.selectByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myMarketCommodityMapper.count(sid, "%" + search + "%");
        } else {
            TCommodityStorageExample example = new TCommodityStorageExample();
            example.or().andSidEqualTo(sid);
            return (int) commodityStorageMapper.countByExample(example);
        }
    }

    // aid，asid是在子查询里使用，所以total不用
    public List<TMarketCommodity> pagination(int sid, int aid, int asid, int page, int limit, String search) {
        if (null != search) {
            return myMarketCommodityMapper.pagination((page - 1) * limit, limit, sid, aid, asid, "%" + search + "%");
        } else {
            return myMarketCommodityMapper.pagination((page - 1) * limit, limit, sid, aid, asid, null);
        }
    }

    public boolean checkByAid(int aid) {
        TMarketCommodityExample example = new TMarketCommodityExample();
        example.or().andAidEqualTo(aid);
        return null != marketCommodityMapper.selectOneByExample(example);
    }

    public boolean checkByAsid(int asid) {
        TMarketCommodityExample example = new TMarketCommodityExample();
        example.or().andAsidEqualTo(asid);
        return null != marketCommodityMapper.selectOneByExample(example);
    }

    public boolean update(TMarketCommodity row) {
        delete(row.getSid(), row.getAid(), row.getAsid(), row.getCid());
        if (marketCommodityMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getAid(), row.getAsid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int aid, int asid, int cid) {
        TMarketCommodity marketCommodity = find(sid, aid, asid, cid);
        if (null == marketCommodity) {
            return false;
        }
        delCache(joinKey(sid, aid, asid, cid));
        return marketCommodityMapper.deleteByPrimaryKey(marketCommodity.getId()) > 0;
    }
}
