package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockDayMapper;
import com.cxb.storehelperserver.model.TStockDay;
import com.cxb.storehelperserver.model.TStockDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 库存日快照仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockDayRepository extends BaseRepository<TStockDay> {
    @Resource
    private TStockDayMapper stockDayMapper;

    @Resource
    private MyStockDayMapper myStockDayMapper;

    public StockDayRepository() {
        init("stockDay::");
    }

    public TStockDay find(int sid, int id, Date date) {
        TStockDayExample example = new TStockDayExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id).andCdateEqualTo(date);
        return stockDayMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, Date start, Date end) {
        return myStockDayMapper.selectReport(gid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int total(int gid, int sid, Date date, String search) {
        if (null != search) {
            return myStockDayMapper.count(gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockDayExample example = new TStockDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockDayMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockDayMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockDayMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockDay row) {
        return stockDayMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockDayExample example = new TStockDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockDayMapper.deleteByExample(example) > 0;
    }
}
