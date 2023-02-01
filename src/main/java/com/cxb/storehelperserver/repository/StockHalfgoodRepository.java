package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockHalfgoodDayMapper;
import com.cxb.storehelperserver.model.TStockHalfgoodDay;
import com.cxb.storehelperserver.model.TStockHalfgoodDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockHalfgoodDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockHalfgood;
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
public class StockHalfgoodRepository extends BaseRepository<TStockHalfgoodDay> {
    @Resource
    private TStockHalfgoodDayMapper stockHalfgoodDayMapper;

    @Resource
    private MyStockHalfgoodDayMapper myStockHalfgoodDayMapper;

    public StockHalfgoodRepository() {
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

    public int total(int sid, Date date, String search) {
        if (null != search) {
            return myStockHalfgoodDayMapper.countByExample(sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockHalfgoodDayExample example = new TStockHalfgoodDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockHalfgoodDayMapper.countByExample(example);
        }
    }

    public List<MyStockHalfgood> pagination(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockHalfgoodDayMapper.selectByExample((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockHalfgoodDayMapper.selectByExample((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockHalfgoodDay row) {
        return stockHalfgoodDayMapper.insert(row) > 0;
    }

    public boolean update(TStockHalfgoodDay row) {
        return stockHalfgoodDayMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockHalfgoodDayExample example = new TStockHalfgoodDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockHalfgoodDayMapper.deleteByExample(example) > 0;
    }
}
