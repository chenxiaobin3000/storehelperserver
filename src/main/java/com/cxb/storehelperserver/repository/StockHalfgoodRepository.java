package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockHalfgoodMapper;
import com.cxb.storehelperserver.model.TStockHalfgood;
import com.cxb.storehelperserver.model.TStockHalfgoodExample;
import com.cxb.storehelperserver.repository.mapper.MyStockHalfgoodMapper;
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
public class StockHalfgoodRepository extends BaseRepository<TStockHalfgood> {
    @Resource
    private TStockHalfgoodMapper stockHalfgoodMapper;

    @Resource
    private MyStockHalfgoodMapper myStockHalfgoodMapper;

    public StockHalfgoodRepository() {
        init("stockH::");
    }

    public TStockHalfgood find(int sid, int id) {
        TStockHalfgood halfgood = getCache(joinKey(sid, id), TStockHalfgood.class);
        if (null != halfgood) {
            return halfgood;
        }

        TStockHalfgoodExample example = new TStockHalfgoodExample();
        example.or().andSidEqualTo(sid).andHidEqualTo(id);
        return stockHalfgoodMapper.selectOneByExample(example);
    }

    public int total(int sid, Date date, String search) {
        if (null != search) {
            return myStockHalfgoodMapper.countByExample(sid, date, "%" + search + "%");
        } else {
            TStockHalfgoodExample example = new TStockHalfgoodExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockHalfgoodMapper.countByExample(example);
        }
    }

    public List<TStockHalfgood> pagination(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockHalfgoodMapper.selectByExample((page - 1) * limit, limit, sid, date, "%" + search + "%");
        } else {
            return myStockHalfgoodMapper.selectByExample((page - 1) * limit, limit, sid, date, null);
        }
    }

    public boolean insert(TStockHalfgood row) {
        return stockHalfgoodMapper.insert(row) > 0;
    }

    public boolean update(TStockHalfgood row) {
        return stockHalfgoodMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int id) {
        return stockHalfgoodMapper.deleteByPrimaryKey(id) > 0;
    }
}
