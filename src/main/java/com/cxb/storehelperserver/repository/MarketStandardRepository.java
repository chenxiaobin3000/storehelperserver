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

    public TMarketStandard find(int sid, int aid, int asid, int cid) {
        TMarketStandard marketStandard = getCache(joinKey(sid, aid, asid, cid), TMarketStandard.class);
        if (null != marketStandard) {
            return marketStandard;
        }

        // 缓存没有就查询数据库
        TMarketStandardExample example = new TMarketStandardExample();
        example.or().andSidEqualTo(sid).andAidEqualTo(aid).andAsidEqualTo(asid).andCidEqualTo(cid);
        marketStandard = marketStandardMapper.selectOneByExample(example);
        if (null != marketStandard) {
            setCache(joinKey(sid, aid, asid, cid), marketStandard);
        }
        return marketStandard;
    }

    public int total(int sid, int aid, int asid, String search) {
        if (null != search) {
            return myMarketStandardMapper.count(sid, aid, asid, "%" + search + "%");
        } else {
            return myMarketStandardMapper.count(sid, aid, asid, null);
        }
    }

    public List<TMarketStandard> pagination(int sid, int aid, int asid, int page, int limit, String search) {
        if (null != search) {
            return myMarketStandardMapper.pagination((page - 1) * limit, limit, sid, aid, asid, "%" + search + "%");
        } else {
            return myMarketStandardMapper.pagination((page - 1) * limit, limit, sid, aid, asid, null);
        }
    }

    public boolean update(TMarketStandard row) {
        delete(row.getSid(), row.getAid(), row.getAsid(), row.getCid());
        if (marketStandardMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getAid(), row.getAsid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int aid, int asid, int cid) {
        TMarketStandard marketStandard = find(sid, aid, asid, cid);
        if (null == marketStandard) {
            return false;
        }
        delCache(joinKey(sid, aid, asid, cid));
        return marketStandardMapper.deleteByPrimaryKey(marketStandard.getId()) > 0;
    }
}
