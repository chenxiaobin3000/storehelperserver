package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockOriginalMapper;
import com.cxb.storehelperserver.model.TStockOriginal;
import com.cxb.storehelperserver.model.TStockOriginalExample;
import com.cxb.storehelperserver.repository.mapper.MyStockOriginalMapper;
import com.cxb.storehelperserver.repository.model.MyStockOriginal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 仓储原料仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockOriginalRepository extends BaseRepository<TStockOriginal> {
    @Resource
    private TStockOriginalMapper stockOriginalMapper;

    @Resource
    private MyStockOriginalMapper myStockOriginalMapper;

    public StockOriginalRepository() {
        init("stockO::");
    }

    public TStockOriginal find(int sid, int id, Date date) {
        TStockOriginalExample example = new TStockOriginalExample();
        example.or().andSidEqualTo(sid).andOidEqualTo(id).andCdateEqualTo(date);
        return stockOriginalMapper.selectOneByExample(example);
    }

    public int total(int sid, Date date, String search) {
        if (null != search) {
            return myStockOriginalMapper.countByExample(sid, date, "%" + search + "%");
        } else {
            TStockOriginalExample example = new TStockOriginalExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockOriginalMapper.countByExample(example);
        }
    }

    public List<MyStockOriginal> pagination(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockOriginalMapper.selectByExample((page - 1) * limit, limit, sid, date, "%" + search + "%");
        } else {
            return myStockOriginalMapper.selectByExample((page - 1) * limit, limit, sid, date, null);
        }
    }

    public boolean insert(TStockOriginal row) {
        return stockOriginalMapper.insert(row) > 0;
    }

    public boolean update(TStockOriginal row) {
        return stockOriginalMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int sid, int oid, Date date) {
        TStockOriginalExample example = new TStockOriginalExample();
        example.or().andSidEqualTo(sid).andOidEqualTo(oid).andCdateGreaterThanOrEqualTo(date);
        return stockOriginalMapper.deleteByExample(example) > 0;
    }
}
