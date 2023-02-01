package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockStandardDayMapper;
import com.cxb.storehelperserver.model.TStockStandardDay;
import com.cxb.storehelperserver.model.TStockStandardDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockStandardDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockStandard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 仓储标品仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockStandardRepository extends BaseRepository<TStockStandardDay> {
    @Resource
    private TStockStandardDayMapper stockStandardDayMapper;

    @Resource
    private MyStockStandardDayMapper myStockStandardDayMapper;

    public StockStandardRepository() {
        init("stockSD::");
    }

    public TStockStandardDay find(int sid, int id, Date date) {
        TStockStandardDayExample example = new TStockStandardDayExample();
        example.or().andSidEqualTo(sid).andStidEqualTo(id).andCdateEqualTo(date);
        return stockStandardDayMapper.selectOneByExample(example);
    }

    public TStockStandardDay findLast(int sid, int stid) {
        TStockStandardDayExample example = new TStockStandardDayExample();
        if (0 == stid) {
            example.or().andSidEqualTo(sid);
        } else {
            example.or().andSidEqualTo(sid).andStidEqualTo(stid);
        }
        example.setOrderByClause("cdate desc");
        return stockStandardDayMapper.selectOneByExample(example);
    }

    public int total(int sid, Date date, String search) {
        if (null != search) {
            return myStockStandardDayMapper.countByExample(sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockStandardDayExample example = new TStockStandardDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockStandardDayMapper.countByExample(example);
        }
    }

    public List<MyStockStandard> pagination(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockStandardDayMapper.selectByExample((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockStandardDayMapper.selectByExample((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockStandardDay row) {
        return stockStandardDayMapper.insert(row) > 0;
    }

    public boolean update(TStockStandardDay row) {
        return stockStandardDayMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockStandardDayExample example = new TStockStandardDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockStandardDayMapper.deleteByExample(example) > 0;
    }
}
