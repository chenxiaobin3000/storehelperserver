package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockOriginalDayMapper;
import com.cxb.storehelperserver.model.TStockOriginalDay;
import com.cxb.storehelperserver.model.TStockOriginalDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockOriginalDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockOriginal;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 仓储原料仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockOriginalDayRepository extends BaseRepository<TStockOriginalDay> {
    @Resource
    private TStockOriginalDayMapper stockOriginalDayMapper;

    @Resource
    private MyStockOriginalDayMapper myStockOriginalDayMapper;

    public StockOriginalDayRepository() {
        init("stockOD::");
    }

    public TStockOriginalDay find(int sid, int id, Date date) {
        TStockOriginalDayExample example = new TStockOriginalDayExample();
        example.or().andSidEqualTo(sid).andOidEqualTo(id).andCdateEqualTo(date);
        return stockOriginalDayMapper.selectOneByExample(example);
    }

    public TStockOriginalDay findLast(int sid, int oid) {
        TStockOriginalDayExample example = new TStockOriginalDayExample();
        if (0 == oid) {
            example.or().andSidEqualTo(sid);
        } else {
            example.or().andSidEqualTo(sid).andOidEqualTo(oid);
        }
        example.setOrderByClause("cdate desc");
        return stockOriginalDayMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, Date start, Date end) {
        return myStockOriginalDayMapper.selectReport(gid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int totalBySid(int sid, Date date, String search) {
        if (null != search) {
            return myStockOriginalDayMapper.countBySid(sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockOriginalDayExample example = new TStockOriginalDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockOriginalDayMapper.countByExample(example);
        }
    }

    public List<MyStockOriginal> paginationBySid(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockOriginalDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockOriginalDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public int totalByGid(int gid, Date date, String search) {
        if (null != search) {
            return myStockOriginalDayMapper.countByGid(gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockOriginalDayExample example = new TStockOriginalDayExample();
            example.or().andGidEqualTo(gid).andCdateEqualTo(date);
            return (int) stockOriginalDayMapper.countByExample(example);
        }
    }

    public List<MyStockOriginal> paginationByGid(int gid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockOriginalDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockOriginalDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockOriginalDay row) {
        return stockOriginalDayMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockOriginalDayExample example = new TStockOriginalDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockOriginalDayMapper.deleteByExample(example) > 0;
    }
}
