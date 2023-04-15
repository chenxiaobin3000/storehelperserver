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
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;

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

    public TStockDay find(int sid, int ctype, int cid, Date date) {
        TStockDayExample example = new TStockDayExample();
        example.or().andSidEqualTo(sid).andCtypeEqualTo(ctype).andCidEqualTo(cid).andCdateEqualTo(date);
        return stockDayMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, int sid, int ctype, Date start, Date end) {
        return myStockDayMapper.selectReport(gid, sid, ctype, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public List<MyStockCommodity> pagination(int sid, int page, int limit, int ctype, Date date, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        switch (CommodityType.valueOf(ctype)) {
            case COMMODITY:
                return myStockDayMapper.pagination_commodity((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), key);
            case HALFGOOD:
                return myStockDayMapper.pagination_halfgood((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), key);
            case ORIGINAL:
                return myStockDayMapper.pagination_original((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), key);
            case STANDARD:
                return myStockDayMapper.pagination_standard((page - 1) * limit, limit, sid, new java.sql.Date(date.getTime()), key);
            default:
                return null;
        }
    }

    public boolean insert(int gid, int sid, int ctype, int cid, BigDecimal price, int weight, int value, Date cdate) {
        TStockDay row = new TStockDay();
        row.setGid(gid);
        row.setSid(sid);
        row.setCtype(ctype);
        row.setCid(cid);
        row.setPrice(price);
        row.setWeight(weight);
        row.setValue(value);
        row.setCdate(cdate);
        return stockDayMapper.insert(row) > 0;
    }
}
