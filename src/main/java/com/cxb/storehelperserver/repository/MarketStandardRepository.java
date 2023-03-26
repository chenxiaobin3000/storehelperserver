package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketStandardMapper;
import com.cxb.storehelperserver.model.TMarketStandard;
import com.cxb.storehelperserver.model.TMarketStandardExample;
import com.cxb.storehelperserver.repository.mapper.MyMarketStandardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 市场标品对接仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketStandardRepository extends BaseRepository<TMarketStandard> {
    @Resource
    private TMarketStandardMapper marketStandardMapper;

    @Resource
    private MyMarketStandardMapper myMarketStandardMapper;

    public MarketStandardRepository() {
        init("marketStan::");
    }

    public TMarketStandard find(int sid, int mid, int cid) {
        TMarketStandard marketStandard = getCache(joinKey(sid, mid, cid), TMarketStandard.class);
        if (null != marketStandard) {
            return marketStandard;
        }

        // 缓存没有就查询数据库
        TMarketStandardExample example = new TMarketStandardExample();
        example.or().andSidEqualTo(sid).andMidEqualTo(mid).andCidEqualTo(cid);
        marketStandard = marketStandardMapper.selectOneByExample(example);
        if (null != marketStandard) {
            setCache(joinKey(sid, mid, cid), marketStandard);
        }
        return marketStandard;
    }

    public int total(int sid, int mid, String search) {
        if (null != search) {
            return myMarketStandardMapper.count(sid, mid, "%" + search + "%");
        } else {
            return myMarketStandardMapper.count(sid, mid, null);
        }
    }

    public List<TMarketStandard> pagination(int sid, int mid, int page, int limit, String search) {
        if (null != search) {
            return myMarketStandardMapper.pagination((page - 1) * limit, limit, sid, mid, "%" + search + "%");
        } else {
            return myMarketStandardMapper.pagination((page - 1) * limit, limit, sid, mid, null);
        }
    }

    public boolean update(TMarketStandard row) {
        delete(row.getSid(), row.getMid(), row.getCid());
        if (marketStandardMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getMid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int mid, int cid) {
        TMarketStandard marketStandard = find(sid, mid, cid);
        if (null == marketStandard) {
            return false;
        }
        delCache(joinKey(sid, mid, cid));
        return marketStandardMapper.deleteByPrimaryKey(marketStandard.getId()) > 0;
    }
}
