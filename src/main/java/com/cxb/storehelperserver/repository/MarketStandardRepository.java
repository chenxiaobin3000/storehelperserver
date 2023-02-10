package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketStandardMapper;
import com.cxb.storehelperserver.model.TMarketStandard;
import com.cxb.storehelperserver.model.TMarketStandardExample;
import com.cxb.storehelperserver.repository.mapper.MyMarketStandardMapper;
import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
import com.cxb.storehelperserver.repository.model.MyMarketDetailCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
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

    public int total(int gid, int mid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TMarketStandardExample example = new TMarketStandardExample();
            example.or().andGidEqualTo(gid).andMidEqualTo(mid).andNameLike("%" + search + "%");
            return (int) marketStandardMapper.countByExample(example);
        } else {
            int total = getTotalCache(joinKey(gid, mid));
            if (0 != total) {
                return total;
            }
            TMarketStandardExample example = new TMarketStandardExample();
            example.or().andGidEqualTo(gid).andMidEqualTo(mid);
            total = (int) marketStandardMapper.countByExample(example);
            setTotalCache(joinKey(gid, mid), total);
            return total;
        }
    }

    public List<MyMarketCommodity> pagination(int gid, int page, int limit, int mid, String search) {
        if (null != search) {
            return myMarketStandardMapper.select((page - 1) * limit, limit, gid, mid, "%" + search + "%");
        } else {
            return myMarketStandardMapper.select((page - 1) * limit, limit, gid, mid, null);
        }
    }

    public List<MyMarketDetailCommodity> paginationDetail(int gid, int page, int limit, int mid, Date date, String search) {
        if (null != search) {
            return myMarketStandardMapper.selectDetail((page - 1) * limit, limit, gid, mid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myMarketStandardMapper.selectDetail((page - 1) * limit, limit, gid, mid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean update(TMarketStandard row) {
        delete(row.getGid(), row.getMid(), row.getCid());
        if (marketStandardMapper.insert(row) > 0) {
            setCache(joinKey(row.getGid(), row.getMid(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getMid()));
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
        delTotalCache(joinKey(gid, mid));
        return marketStandardMapper.deleteByPrimaryKey(marketStandard.getId()) > 0;
    }
}
