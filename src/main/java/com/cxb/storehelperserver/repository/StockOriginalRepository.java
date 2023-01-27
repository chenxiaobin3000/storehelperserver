package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockOriginalMapper;
import com.cxb.storehelperserver.model.TStockOriginal;
import com.cxb.storehelperserver.model.TStockOriginalExample;
import com.cxb.storehelperserver.repository.mapper.MyStockOriginalMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储原料仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockOriginalRepository {
    @Resource
    private TStockOriginalMapper stockOriginalMapper;

    @Resource
    private MyStockOriginalMapper myStockOriginalMapper;

    public TStockOriginal find(int sid, int id) {
        TStockOriginalExample example = new TStockOriginalExample();
        example.or().andSidEqualTo(sid).andOidEqualTo(id);
        return stockOriginalMapper.selectOneByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myStockOriginalMapper.countByExample(sid, "%" + search + "%");
        } else {
            TStockOriginalExample example = new TStockOriginalExample();
            example.or().andSidEqualTo(sid);
            return (int) stockOriginalMapper.countByExample(example);
        }
    }

    public List<TStockOriginal> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStockOriginalMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStockOriginalMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStockOriginal row) {
        return stockOriginalMapper.insert(row) > 0;
    }

    public boolean update(TStockOriginal row) {
        return stockOriginalMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int id) {
        return stockOriginalMapper.deleteByPrimaryKey(id) > 0;
    }
}
