package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockCloudDayMapper;
import com.cxb.storehelperserver.model.TStockCloudDay;
import com.cxb.storehelperserver.model.TStockCloudDayExample;
import com.cxb.storehelperserver.repository.mapper.MyStockCloudDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import com.cxb.storehelperserver.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * desc: 库存日快照仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockCloudDayRepository extends BaseRepository<TStockCloudDay> {
    @Resource
    private TStockCloudDayMapper stockCloudDayMapper;

    @Resource
    private MyStockCloudDayMapper myStockCloudDayMapper;

    @Resource
    private DateUtil dateUtil;

    public StockCloudDayRepository() {
        init("stockCloudDay::");
    }

    public TStockCloudDay find(int aid, int cid, Date date) {
        TStockCloudDayExample example = new TStockCloudDayExample();
        example.or().andAidEqualTo(aid).andCidEqualTo(cid).andCdateEqualTo(date);
        return stockCloudDayMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int aid, Date start, Date end) {
        return myStockCloudDayMapper.selectReport(aid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    // 所有有库存的商品数量
    public int total(int aid, Date date, String search) {
        if (null != search) {
            return myStockCloudDayMapper.count(aid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TStockCloudDayExample example = new TStockCloudDayExample();
            example.or().andAidEqualTo(aid).andCdateEqualTo(date);
            return (int) stockCloudDayMapper.countByExample(example);
        }
    }

    // 所有有库存的商品列表
    public List<MyStockCommodity> pagination(int aid, int page, int limit, Date date, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        return myStockCloudDayMapper.pagination((page - 1) * limit, limit, aid, new java.sql.Date(date.getTime()), key);
    }

    // 所有商品列表，库存为0也显示
    public List<MyStockCommodity> paginationAll(int aid, int page, int limit, Date date, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        return myStockCloudDayMapper.pagination_all((page - 1) * limit, limit, aid, new java.sql.Date(date.getTime()), key);
    }

    public boolean insert(int gid, int aid, int cid, BigDecimal price, int value, Date cdate) {
        TStockCloudDay row = new TStockCloudDay();
        row.setGid(gid);
        row.setAid(aid);
        row.setCid(cid);
        row.setPrice(price);
        row.setValue(value);
        row.setCdate(cdate);
        return stockCloudDayMapper.insert(row) > 0;
    }
}
