package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockCommodityMapper;
import com.cxb.storehelperserver.model.TStockCommodity;
import com.cxb.storehelperserver.model.TStockCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyStockCommodityMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 商品库存仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockCommodityRepository extends BaseRepository<TStockCommodity> {
    @Resource
    private TStockCommodityMapper stockCommodityMapper;

    @Resource
    private MyStockCommodityMapper myStockCommodityMapper;

    public StockCommodityRepository() {
        init("stockC::");
    }

    public TStockCommodity find(int sid, int id, Date date) {
        TStockCommodityExample example = new TStockCommodityExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id).andCdateEqualTo(date);
        return stockCommodityMapper.selectOneByExample(example);
    }

    public TStockCommodity findLast(int sid, int cid) {
        TStockCommodityExample example = new TStockCommodityExample();
        if (0 == cid) {
            example.or().andSidEqualTo(sid);
        } else {
            example.or().andSidEqualTo(sid).andCidEqualTo(cid);
        }
        example.setOrderByClause("cdate desc");
        return stockCommodityMapper.selectOneByExample(example);
    }

    public int total(int sid, Date date, String search) {
        if (null != search) {
            return myStockCommodityMapper.countByExample(sid, date, "%" + search + "%");
        } else {
            TStockCommodityExample example = new TStockCommodityExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockCommodityMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockCommodityMapper.selectByExample((page - 1) * limit, limit, sid, date, "%" + search + "%");
        } else {
            return myStockCommodityMapper.selectByExample((page - 1) * limit, limit, sid, date, null);
        }
    }

    public boolean insert(TStockCommodity row) {
        return stockCommodityMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockCommodityExample example = new TStockCommodityExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockCommodityMapper.deleteByExample(example) > 0;
    }
}
