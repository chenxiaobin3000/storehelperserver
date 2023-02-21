package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockWeekMapper;
import com.cxb.storehelperserver.model.TStockWeek;
import com.cxb.storehelperserver.model.TStockWeekExample;
import com.cxb.storehelperserver.repository.mapper.MyStockWeekMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 库存周快照仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockWeekRepository extends BaseRepository<TStockWeek> {
    @Resource
    private TStockWeekMapper stockWeekMapper;

    @Resource
    private MyStockWeekMapper myStockWeekMapper;

    public StockWeekRepository() {
        init("stockWeek::");
    }

    public TStockWeek find(int sid, int id, Date date) {
        TStockWeekExample example = new TStockWeekExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id).andCdateEqualTo(date);
        return stockWeekMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, Date start, Date end) {
        return myStockWeekMapper.selectReport(gid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int total(int gid, int sid, Date date, String search) {
        if (null != search) {
            return myStockWeekMapper.count(gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockWeekExample example = new TStockWeekExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockWeekMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockWeekMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockWeekMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockWeek row) {
        return stockWeekMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockWeekExample example = new TStockWeekExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockWeekMapper.deleteByExample(example) > 0;
    }
}
