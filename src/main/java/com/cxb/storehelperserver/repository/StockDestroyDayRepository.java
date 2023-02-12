package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockDestroyDayMapper;
import com.cxb.storehelperserver.model.TStockDestroyDay;
import com.cxb.storehelperserver.model.TStockDestroyDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockDestroyDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockDestroy;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 仓储废料仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockDestroyDayRepository extends BaseRepository<TStockDestroyDay> {
    @Resource
    private TStockDestroyDayMapper stockDestroyDayMapper;

    @Resource
    private MyStockDestroyDayMapper myStockDestroyDayMapper;

    public StockDestroyDayRepository() {
        init("stockDD::");
    }

    public TStockDestroyDay find(int sid, int id, Date date) {
        TStockDestroyDayExample example = new TStockDestroyDayExample();
        example.or().andSidEqualTo(sid).andDidEqualTo(id).andCdateEqualTo(date);
        return stockDestroyDayMapper.selectOneByExample(example);
    }

    public TStockDestroyDay findLast(int sid, int did) {
        TStockDestroyDayExample example = new TStockDestroyDayExample();
        if (0 == did) {
            example.or().andSidEqualTo(sid);
        } else {
            example.or().andSidEqualTo(sid).andDidEqualTo(did);
        }
        example.setOrderByClause("cdate desc");
        return stockDestroyDayMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, Date start, Date end) {
        return myStockDestroyDayMapper.selectReport(gid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int totalBySid(int sid, Date date, String search) {
        if (null != search) {
            return myStockDestroyDayMapper.countBySid(sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockDestroyDayExample example = new TStockDestroyDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockDestroyDayMapper.countByExample(example);
        }
    }

    public List<MyStockDestroy> paginationBySid(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockDestroyDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockDestroyDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public int totalByGid(int gid, Date date, String search) {
        if (null != search) {
            return myStockDestroyDayMapper.countByGid(gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockDestroyDayExample example = new TStockDestroyDayExample();
            example.or().andGidEqualTo(gid).andCdateEqualTo(date);
            return (int) stockDestroyDayMapper.countByExample(example);
        }
    }

    public List<MyStockDestroy> paginationByGid(int gid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockDestroyDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockDestroyDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockDestroyDay row) {
        return stockDestroyDayMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockDestroyDayExample example = new TStockDestroyDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockDestroyDayMapper.deleteByExample(example) > 0;
    }
}
