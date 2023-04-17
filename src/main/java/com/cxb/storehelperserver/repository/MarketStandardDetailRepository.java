package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketStandardDetailMapper;
import com.cxb.storehelperserver.model.TMarketStandardDetail;
import com.cxb.storehelperserver.repository.mapper.MyMarketMapper;
import com.cxb.storehelperserver.repository.mapper.MyMarketStandardMapper;
import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
import com.cxb.storehelperserver.repository.model.MyMarketReport;
import com.cxb.storehelperserver.repository.model.MyMarketSaleInfo;
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

    @Resource
    private MyMarketStandardMapper myMarketStandardMapper;

    @Resource
    private MyMarketMapper myMarketMapper;

    public MarketStandardDetailRepository() {
        init("marketStanDetail::");
    }

    public TMarketStandardDetail find(int id) {
        TMarketStandardDetail detail = getCache(id, TMarketStandardDetail.class);
        if (null != detail) {
            return detail;
        }

        // 缓存没有就查询数据库
        detail = marketStandardDetailMapper.selectByPrimaryKey(id);
        if (null != detail) {
            setCache(id, detail);
        }
        return detail;
    }

    public List<MyMarketReport> findByDate(int gid, int mid, Date start, Date end) {
        return myMarketMapper.select_standard(gid, mid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public List<MyMarketSaleInfo> findInCids(int gid, int mid, List<Integer> cids) {
        return myMarketMapper.selectInCids_standard(gid, mid, cids);
    }

    public int total(int sid, int aid, int asid, String search) {
        if (null != search) {
            return myMarketStandardMapper.countDetail(sid, aid, asid, "%" + search + "%");
        } else {
            return myMarketStandardMapper.countDetail(sid, aid, asid, null);
        }
    }

    public List<MyMarketCommodity> pagination(int sid, int aid, int asid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myMarketStandardMapper.paginationDetail((page - 1) * limit, limit, sid, aid, asid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myMarketStandardMapper.paginationDetail((page - 1) * limit, limit, sid, aid, asid, new java.sql.Date(date.getTime()), null);
        }
    }

    public List<MyMarketCommodity> sale(int sid, int aid, int asid, Date date) {
        return myMarketStandardMapper.sale(sid, aid, asid, new java.sql.Date(date.getTime()));
    }

    public boolean insert(TMarketStandardDetail row) {
        if (marketStandardDetailMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TMarketStandardDetail row) {
        if (marketStandardDetailMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return marketStandardDetailMapper.deleteByPrimaryKey(id) > 0;
    }
}
