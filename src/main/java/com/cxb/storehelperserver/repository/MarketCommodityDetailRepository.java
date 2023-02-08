package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketCommodityDetailMapper;
import com.cxb.storehelperserver.model.TCommodity;
import com.cxb.storehelperserver.model.TMarketCommodityDetail;
import com.cxb.storehelperserver.model.TMarketCommodityDetailExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 市场商品销售明细仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketCommodityDetailRepository extends BaseRepository<TMarketCommodityDetail> {
    @Resource
    private TMarketCommodityDetailMapper marketCommodityDetailMapper;

    public MarketCommodityDetailRepository() {
        init("marketCommDetail::");
    }

    public int total(int gid, int mid, String search) {
        int total = getTotalCache(joinKey(gid, mid));
        if (0 != total) {
            return total;
        }
        TMarketCommodityDetailExample example = new TMarketCommodityDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        total = (int) marketCommodityDetailMapper.countByExample(example);
        setTotalCache(joinKey(gid, mid), total);
        return total;
    }

    public List<TMarketCommodityDetail> pagination(int gid, int page, int limit, int mid, String search) {
        TMarketCommodityDetailExample example = new TMarketCommodityDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return marketCommodityDetailMapper.selectByExample(example);
    }

    public boolean update(TMarketCommodityDetail row) {
        delete(row.getGid(), row.getMid(), row.getCid(), row.getCdate());
        if (marketCommodityDetailMapper.insert(row) > 0) {
            delTotalCache(joinKey(row.getGid(), row.getMid()));
            return true;
        }
        return false;
    }

    public boolean delete(int gid, int mid, int cid, Date date) {
        delTotalCache(joinKey(gid, mid));
        TMarketCommodityDetailExample example = new TMarketCommodityDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid).andCidEqualTo(cid).andCdateEqualTo(date);
        return marketCommodityDetailMapper.deleteByExample(example) > 0;
    }
}
