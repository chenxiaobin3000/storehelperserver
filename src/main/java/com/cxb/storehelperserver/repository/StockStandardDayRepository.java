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
public class StockStandardDayRepository extends BaseRepository<TStockStandardDay> {
    @Resource
    private TStockStandardDayMapper stockStandardDayMapper;

    @Resource
    private MyStockStandardDayMapper myStockStandardDayMapper;

    public StockStandardDayRepository() {
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

    public int totalBySid(int sid, Date date, String search) {
        if (null != search) {
            return myStockStandardDayMapper.countBySid(sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockStandardDayExample example = new TStockStandardDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockStandardDayMapper.countByExample(example);
        }
    }

    public List<MyStockStandard> paginationBySid(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockStandardDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockStandardDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public int totalByGid(int gid, Date date, String search) {
        if (null != search) {
            return myStockStandardDayMapper.countByGid(gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockStandardDayExample example = new TStockStandardDayExample();
            example.or().andGidEqualTo(gid).andCdateEqualTo(date);
            return (int) stockStandardDayMapper.countByExample(example);
        }
    }

    public List<MyStockStandard> paginationByGid(int gid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockStandardDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockStandardDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockStandardDay row) {
        return stockStandardDayMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockStandardDayExample example = new TStockStandardDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockStandardDayMapper.deleteByExample(example) > 0;
    }
}
