package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockMonthMapper;
import com.cxb.storehelperserver.model.TStockMonth;
import com.cxb.storehelperserver.model.TStockMonthExample;
import com.cxb.storehelperserver.repository.mapper.MyStockMonthMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 库存月快照仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockMonthRepository extends BaseRepository<TStockMonth> {
    @Resource
    private TStockMonthMapper stockMonthMapper;

    @Resource
    private MyStockMonthMapper myStockMonthMapper;

    public StockMonthRepository() {
        init("stockMonth::");
    }

    public TStockMonth find(int sid, int id, Date date) {
        TStockMonthExample example = new TStockMonthExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id).andCdateEqualTo(date);
        return stockMonthMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, int sid, int ctype, Date start, Date end) {
        return myStockMonthMapper.selectReport(gid, sid, ctype, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int total(int gid, int sid, Date date, String search) {
        if (null != search) {
            return myStockMonthMapper.count(gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockMonthExample example = new TStockMonthExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockMonthMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockMonthMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockMonthMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockMonth row) {
        return stockMonthMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockMonthExample example = new TStockMonthExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockMonthMapper.deleteByExample(example) > 0;
    }
}
