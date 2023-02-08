package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketStandardMapper;
import com.cxb.storehelperserver.model.TMarketStandard;
import com.cxb.storehelperserver.model.TMarketStandardExample;
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

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TMarketStandardExample example = new TMarketStandardExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) marketStandardMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TMarketStandardExample example = new TMarketStandardExample();
            example.or().andGidEqualTo(gid);
            total = (int) marketStandardMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TMarketStandard> pagination(int gid, int page, int limit, String search) {
        TMarketStandardExample example = new TMarketStandardExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return marketStandardMapper.selectByExample(example);
    }

    public boolean update(TMarketStandard row) {
        delete(row.getGid(), row.getMid(), row.getCid());
        if (marketStandardMapper.insert(row) > 0) {
            setCache(joinKey(row.getGid(), row.getMid(), row.getCid()), row);
            delTotalCache(row.getGid());
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
        delTotalCache(marketStandard.getGid());
        return marketStandardMapper.deleteByPrimaryKey(marketStandard.getId()) > 0;
    }
}
