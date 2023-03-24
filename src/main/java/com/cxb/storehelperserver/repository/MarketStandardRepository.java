package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketStandardMapper;
import com.cxb.storehelperserver.model.TMarketStandard;
import com.cxb.storehelperserver.model.TMarketStandardExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

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

    public MarketStandardRepository() {
        init("marketStan::");
    }

    public TMarketStandard find(int gid, int mid, int cid) {
        TMarketStandard marketStandard = getCache(joinKey(gid, mid, cid), TMarketStandard.class);
        if (null != marketStandard) {
            return marketStandard;
        }

        // 缓存没有就查询数据库
        TMarketStandardExample example = new TMarketStandardExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid).andCidEqualTo(cid);
        marketStandard = marketStandardMapper.selectOneByExample(example);
        if (null != marketStandard) {
            setCache(joinKey(gid, mid, cid), marketStandard);
        }
        return marketStandard;
    }

    public boolean update(TMarketStandard row) {
        delete(row.getGid(), row.getMid(), row.getCid());
        if (marketStandardMapper.insert(row) > 0) {
            setCache(joinKey(row.getGid(), row.getMid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int gid, int mid, int cid) {
        TMarketStandard marketStandard = find(gid, mid, cid);
        if (null == marketStandard) {
            return false;
        }
        delCache(joinKey(gid, mid, cid));
        return marketStandardMapper.deleteByPrimaryKey(marketStandard.getId()) > 0;
    }
}
