package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketStandardDetailMapper;
import com.cxb.storehelperserver.model.TMarketStandard;
import com.cxb.storehelperserver.model.TMarketStandardDetail;
import com.cxb.storehelperserver.model.TMarketStandardDetailExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 市场标品销售明细仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketStandardDetailRepository extends BaseRepository<TMarketStandardDetail> {
    @Resource
    private TMarketStandardDetailMapper marketStandardDetailMapper;

    public MarketStandardDetailRepository() {
        init("marketStanDetail::");
    }

    public int total(int gid, int mid, String search) {
        int total = getTotalCache(joinKey(gid, mid));
        if (0 != total) {
            return total;
        }
        TMarketStandardDetailExample example = new TMarketStandardDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        total = (int) marketStandardDetailMapper.countByExample(example);
        setTotalCache(joinKey(gid, mid), total);
        return total;
    }

    public List<TMarketStandardDetail> pagination(int gid, int page, int limit, int mid, String search) {
        TMarketStandardDetailExample example = new TMarketStandardDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return marketStandardDetailMapper.selectByExample(example);
    }

    public boolean update(TMarketStandardDetail row) {
        delete(row.getGid(), row.getMid(), row.getCid(), row.getCdate());
        if (marketStandardDetailMapper.insert(row) > 0) {
            delTotalCache(joinKey(row.getGid(), row.getMid()));
            return true;
        }
        return false;
    }

    public boolean delete(int gid, int mid, int cid, Date date) {
        delTotalCache(joinKey(gid, mid));
        TMarketStandardDetailExample example = new TMarketStandardDetailExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid).andCidEqualTo(cid).andCdateEqualTo(date);
        return marketStandardDetailMapper.deleteByExample(example) > 0;
    }
}
