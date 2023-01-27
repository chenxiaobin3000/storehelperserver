package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockCommodityMapper;
import com.cxb.storehelperserver.model.TStockCommodity;
import com.cxb.storehelperserver.model.TStockCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyStockCommodityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 商品库存仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockCommodityRepository {
    @Resource
    private TStockCommodityMapper stockCommodityMapper;

    @Resource
    private MyStockCommodityMapper myStockCommodityMapper;

    public TStockCommodity find(int sid, int id) {
        TStockCommodityExample example = new TStockCommodityExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id);
        return stockCommodityMapper.selectOneByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myStockCommodityMapper.countByExample(sid, "%" + search + "%");
        } else {
            TStockCommodityExample example = new TStockCommodityExample();
            example.or().andSidEqualTo(sid);
            return (int) stockCommodityMapper.countByExample(example);
        }
    }

    public List<TStockCommodity> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStockCommodityMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStockCommodityMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStockCommodity row) {
        return stockCommodityMapper.insert(row) > 0;
    }

    public boolean update(TStockCommodity row) {
        return stockCommodityMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int id) {
        return stockCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
