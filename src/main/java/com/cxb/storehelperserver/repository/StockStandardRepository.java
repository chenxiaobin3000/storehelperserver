package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockStandardMapper;
import com.cxb.storehelperserver.model.TStockStandard;
import com.cxb.storehelperserver.model.TStockStandardExample;
import com.cxb.storehelperserver.repository.mapper.MyStockStandardMapper;
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
public class StockStandardRepository extends BaseRepository<TStockStandard> {
    @Resource
    private TStockStandardMapper stockStandardMapper;

    @Resource
    private MyStockStandardMapper myStockStandardMapper;

    public StockStandardRepository() {
        init("stockS::");
    }

    public TStockStandard find(int sid, int id) {
        TStockStandard standard = getCache(joinKey(sid, id), TStockStandard.class);
        if (null != standard) {
            return standard;
        }

        TStockStandardExample example = new TStockStandardExample();
        example.or().andSidEqualTo(sid).andStidEqualTo(id);
        return stockStandardMapper.selectOneByExample(example);
    }

    public int total(int sid, Date date, String search) {
        if (null != search) {
            return myStockStandardMapper.countByExample(sid, date, "%" + search + "%");
        } else {
            TStockStandardExample example = new TStockStandardExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockStandardMapper.countByExample(example);
        }
    }

    public List<TStockStandard> pagination(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockStandardMapper.selectByExample((page - 1) * limit, limit, sid, date, "%" + search + "%");
        } else {
            return myStockStandardMapper.selectByExample((page - 1) * limit, limit, sid, date, null);
        }
    }

    public boolean insert(TStockStandard row) {
        return stockStandardMapper.insert(row) > 0;
    }

    public boolean update(TStockStandard row) {
        return stockStandardMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int id) {
        return stockStandardMapper.deleteByPrimaryKey(id) > 0;
    }
}
