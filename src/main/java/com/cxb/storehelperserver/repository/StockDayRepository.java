package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockDayMapper;
import com.cxb.storehelperserver.model.TStockDay;
import com.cxb.storehelperserver.model.TStockDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import com.cxb.storehelperserver.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * desc: 库存日快照仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockDayRepository extends BaseRepository<TStockDay> {
    @Resource
    private TStockDayMapper stockDayMapper;

    @Resource
    private MyStockDayMapper myStockDayMapper;

    @Resource
    private DateUtil dateUtil;

    public StockDayRepository() {
        init("stockDay::");
    }

    public TStockDay find(int sid, int cid, Date date) {
        SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
        String dateString = simpleDateFormat.format(date);
        TStockDay day = getCache(joinKey(dateString, sid, cid), TStockDay.class);
        if (null != day) {
            return day;
        }

        TStockDayExample example = new TStockDayExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(cid).andCdateEqualTo(date);
        day = stockDayMapper.selectOneByExample(example);
        if (null != day) {
            setCache(joinKey(dateString, sid, cid), day);
        }
        return day;
    }

    public List<MyStockReport> findReport(int gid, int sid, Date start, Date end) {
        return myStockDayMapper.selectReport(gid, sid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    // 所有有库存的商品数量
    public int total(int sid, Date date, String search) {
        if (null != search) {
            return myStockDayMapper.count(sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockDayExample example = new TStockDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockDayMapper.countByExample(example);
        }
    }

    // 所有有库存的商品列表
    public List<MyStockCommodity> pagination(int sid, int page, int limit, Date date, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        return myStockDayMapper.pagination((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), key);
    }

    // 所有商品列表，库存为0也显示
    public List<MyStockCommodity> paginationAll(int sid, int page, int limit, Date date, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        return myStockDayMapper.pagination_all((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), key);
    }

    public boolean insert(int gid, int sid, int cid, BigDecimal price, int weight, String norm, int value, Date cdate) {
        TStockDay row = new TStockDay();
        row.setGid(gid);
        row.setSid(sid);
        row.setCid(cid);
        row.setPrice(price);
        row.setWeight(weight);
        row.setNorm(norm);
        row.setValue(value);
        row.setCdate(cdate);
        return stockDayMapper.insert(row) > 0;
    }
}
