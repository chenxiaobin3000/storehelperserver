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

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;

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

    public List<MyStockReport> findReport(int gid, int sid, int ctype, Date start, Date end) {
        return myStockMapper.selectReport(gid, sid, ctype, start, end);
    }

    public List<MyStockCommodity> findHistoryAll(int gid, int sid, int ctype, Date start, Date end) {
        switch (CommodityType.valueOf(ctype)) {
            case COMMODITY:
                return myStockMapper.selectHistory_commodity_all(gid, sid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            case HALFGOOD:
                return myStockMapper.selectHistory_halfgood_all(gid, sid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            case ORIGINAL:
                return myStockMapper.selectHistory_original_all(gid, sid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            default:
                return null;
        }
    }

    public List<MyStockCommodity> findHistory(int gid, int sid, int ctype, int cid, Date start, Date end) {
        switch (CommodityType.valueOf(ctype)) {
            case COMMODITY:
                return myStockMapper.selectHistory_commodity(gid, sid, cid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            case HALFGOOD:
                return myStockMapper.selectHistory_halfgood(gid, sid, cid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            case ORIGINAL:
                return myStockMapper.selectHistory_original(gid, sid, cid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            default:
                return null;
        }
    }

    public int total(int gid, int sid, int ctype, Date start, Date end, String search) {
        if (null != search) {
            switch (CommodityType.valueOf(ctype)) {
                case COMMODITY:
                    return myStockMapper.count_commodity(gid, sid, start, end, "%" + search + "%");
                case HALFGOOD:
                    return myStockMapper.count_halfgood(gid, sid, start, end, "%" + search + "%");
                case ORIGINAL:
                    return myStockMapper.count_original(gid, sid, start, end, "%" + search + "%");
                default:
                    return 0;
            }
        } else {
            TStockExample example = new TStockExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(start).andCdateLessThanOrEqualTo(end);
            return (int) stockMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, int ctype, Date start, Date end, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        switch (CommodityType.valueOf(ctype)) {
            case COMMODITY:
                return myStockMapper.pagination_commodity((page - 1) * limit, limit, gid, sid, start, end, key);
            case HALFGOOD:
                return myStockMapper.pagination_halfgood((page - 1) * limit, limit, gid, sid, start, end, key);
            case ORIGINAL:
                return myStockMapper.pagination_original((page - 1) * limit, limit, gid, sid, start, end, key);
            default:
                return null;
        }
    }

    public boolean insert(int gid, int sid, int otype, Integer oid, int ctype, int cid, BigDecimal price, int weight, int value, Date cdate) {
        TStock row = new TStock();
        row.setGid(gid);
        row.setSid(sid);
        row.setOtype(otype);
        row.setOid(null == oid ? 0 : oid);
        row.setCtype(ctype);
        row.setCid(cid);
        row.setPrice(price);
        row.setWeight(weight);
        row.setValue(value);
        row.setCdate(cdate);
        return stockMapper.insert(row) > 0;
    }
}
