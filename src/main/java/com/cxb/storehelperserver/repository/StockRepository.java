package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockMapper;
import com.cxb.storehelperserver.model.TStock;
import com.cxb.storehelperserver.model.TStockExample;
import com.cxb.storehelperserver.repository.mapper.MyStockMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * desc: 仓储库存明细仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockRepository extends BaseRepository<TStock> {
    @Resource
    private TStockMapper stockMapper;

    @Resource
    private MyStockMapper myStockMapper;

    public StockRepository() {
        init("stock::");
    }

    public List<MyStockReport> findReport(int gid, int sid, Date start, Date end) {
        return myStockMapper.selectReport(gid, sid, start, end);
    }

    public List<MyStockCommodity> findHistoryAll(int gid, int sid, Date start, Date end) {
        return myStockMapper.selectHistory_all(gid, sid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public List<MyStockCommodity> findHistory(int gid, int sid, int cid, Date start, Date end) {
        return myStockMapper.selectHistory(gid, sid, cid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int total(int gid, int sid, Date start, Date end, String search) {
        if (null != search) {
            return myStockMapper.count(gid, sid, start, end, "%" + search + "%");
        } else {
            TStockExample example = new TStockExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(start).andCdateLessThanOrEqualTo(end);
            return (int) stockMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, Date start, Date end, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        return myStockMapper.pagination((page - 1) * limit, limit, gid, sid, start, end, key);
    }

    public boolean insert(int gid, int sid, int otype, Integer oid, int cid, BigDecimal price, int weight, String norm, int value, Date cdate) {
        TStock row = new TStock();
        row.setGid(gid);
        row.setSid(sid);
        row.setOtype(otype);
        row.setOid(null == oid ? 0 : oid);
        row.setCid(cid);
        row.setPrice(price);
        row.setWeight(weight);
        row.setNorm(norm);
        row.setValue(value);
        row.setCdate(cdate);
        return stockMapper.insert(row) > 0;
    }
}
