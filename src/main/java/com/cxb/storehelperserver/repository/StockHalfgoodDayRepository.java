package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockHalfgoodDayMapper;
import com.cxb.storehelperserver.model.TStockHalfgoodDay;
import com.cxb.storehelperserver.model.TStockHalfgoodDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockHalfgoodDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockHalfgood;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 仓储半成品仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class StockHalfgoodDayRepository extends BaseRepository<TStockHalfgoodDay> {
    @Resource
    private TStockHalfgoodDayMapper stockHalfgoodDayMapper;

    @Resource
    private MyStockHalfgoodDayMapper myStockHalfgoodDayMapper;

    public StockHalfgoodDayRepository() {
        init("stockHD::");
    }

    public TStockHalfgoodDay find(int sid, int id, Date date) {
        TStockHalfgoodDayExample example = new TStockHalfgoodDayExample();
        example.or().andSidEqualTo(sid).andHidEqualTo(id).andCdateEqualTo(date);
        return stockHalfgoodDayMapper.selectOneByExample(example);
    }

    public TStockHalfgoodDay findLast(int sid, int hid) {
        TStockHalfgoodDayExample example = new TStockHalfgoodDayExample();
        if (0 == hid) {
            example.or().andSidEqualTo(sid);
        } else {
            example.or().andSidEqualTo(sid).andHidEqualTo(hid);
        }
        example.setOrderByClause("cdate desc");
        return stockHalfgoodDayMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, Date start, Date end) {
        return myStockHalfgoodDayMapper.selectReport(gid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int totalBySid(int sid, Date date, String search) {
        if (null != search) {
            return myStockHalfgoodDayMapper.countBySid(sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockHalfgoodDayExample example = new TStockHalfgoodDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockHalfgoodDayMapper.countByExample(example);
        }
    }

    public List<MyStockHalfgood> paginationBySid(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockHalfgoodDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockHalfgoodDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public int totalByGid(int gid, Date date, String search) {
        if (null != search) {
            return myStockHalfgoodDayMapper.countByGid(gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockHalfgoodDayExample example = new TStockHalfgoodDayExample();
            example.or().andGidEqualTo(gid).andCdateEqualTo(date);
            return (int) stockHalfgoodDayMapper.countByExample(example);
        }
    }

    public List<MyStockHalfgood> paginationByGid(int gid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockHalfgoodDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockHalfgoodDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockHalfgoodDay row) {
        return stockHalfgoodDayMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockHalfgoodDayExample example = new TStockHalfgoodDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockHalfgoodDayMapper.deleteByExample(example) > 0;
    }
}
