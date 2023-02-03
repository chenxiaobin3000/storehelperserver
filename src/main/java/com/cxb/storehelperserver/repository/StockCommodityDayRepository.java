package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockCommodityDayMapper;
import com.cxb.storehelperserver.model.TStockCommodityDay;
import com.cxb.storehelperserver.model.TStockCommodityDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockCommodityDayMapper;
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
public class StockCommodityDayRepository extends BaseRepository<TStockCommodityDay> {
    @Resource
    private TStockCommodityDayMapper stockCommodityDayMapper;

    @Resource
    private MyStockCommodityDayMapper myStockCommodityDayMapper;

    public StockCommodityDayRepository() {
        init("stockCD::");
    }

    public TStockCommodityDay find(int sid, int id, Date date) {
        TStockCommodityDayExample example = new TStockCommodityDayExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id).andCdateEqualTo(date);
        return stockCommodityDayMapper.selectOneByExample(example);
    }

    public TStockCommodityDay findLast(int sid, int cid) {
        TStockCommodityDayExample example = new TStockCommodityDayExample();
        if (0 == cid) {
            example.or().andSidEqualTo(sid);
        } else {
            example.or().andSidEqualTo(sid).andCidEqualTo(cid);
        }
        example.setOrderByClause("cdate desc");
        return stockCommodityDayMapper.selectOneByExample(example);
    }

    public int totalBySid(int sid, Date date, String search) {
        if (null != search) {
            return myStockCommodityDayMapper.countBySid(sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockCommodityDayExample example = new TStockCommodityDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockCommodityDayMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> paginationBySid(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockCommodityDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockCommodityDayMapper.selectBySid((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public int totalByGid(int gid, Date date, String search) {
        if (null != search) {
            return myStockCommodityDayMapper.countByGid(gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockCommodityDayExample example = new TStockCommodityDayExample();
            example.or().andGidEqualTo(gid).andCdateEqualTo(date);
            return (int) stockCommodityDayMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> paginationByGid(int gid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockCommodityDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myStockCommodityDayMapper.selectByGid((page - 1) * limit, limit, gid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TStockCommodityDay row) {
        return stockCommodityDayMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockCommodityDayExample example = new TStockCommodityDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockCommodityDayMapper.deleteByExample(example) > 0;
    }
}
