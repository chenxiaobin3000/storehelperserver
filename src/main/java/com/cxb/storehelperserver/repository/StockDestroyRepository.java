package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockDestroyDayMapper;
import com.cxb.storehelperserver.model.TStockDestroyDay;
import com.cxb.storehelperserver.model.TStockDestroyDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockDestroyDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockDestroy;
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
public class StockDestroyRepository extends BaseRepository<TStockDestroyDay> {
    @Resource
    private TStockDestroyDayMapper stockDestroyDayMapper;

    @Resource
    private MyStockDestroyDayMapper myStockDestroyDayMapper;

    public StockDestroyRepository() {
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

    public int total(int sid, Date date, String search) {
        if (null != search) {
            return myStockDestroyDayMapper.countByExample(sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockDestroyDayExample example = new TStockDestroyDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockDestroyDayMapper.countByExample(example);
        }
    }

    public List<MyStockDestroy> pagination(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockDestroyDayMapper.selectByExample((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockDestroyDayMapper.selectByExample((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockDestroyDay row) {
        return stockDestroyDayMapper.insert(row) > 0;
    }

    public boolean update(TStockDestroyDay row) {
        return stockDestroyDayMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockDestroyDayExample example = new TStockDestroyDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockDestroyDayMapper.deleteByExample(example) > 0;
    }
}
