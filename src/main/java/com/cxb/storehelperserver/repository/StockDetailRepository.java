package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockDetailMapper;
import com.cxb.storehelperserver.model.TStockDetail;
import com.cxb.storehelperserver.model.TStockDetailExample;
import com.cxb.storehelperserver.repository.mapper.MyStockDetailMapper;
import com.cxb.storehelperserver.repository.model.MyStockDetail;
import com.cxb.storehelperserver.util.TypeDefine;
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
public class StockDetailRepository extends BaseRepository<TStockDetail> {
    @Resource
    private TStockDetailMapper stockDetailMapper;

    @Resource
    private MyStockDetailMapper myStockDetailMapper;

    public StockDetailRepository() {
        init("stockDetail::");
    }

    public TStockDetail find(int sid, int cid) {
        TStockDetail stock = getCache(joinKey(sid, cid), TStockDetail.class);
        if (null != stock) {
            return stock;
        }

        // 缓存没有就查询数据库
        TStockDetailExample example = new TStockDetailExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(cid);
        stock = stockDetailMapper.selectOneByExample(example);
        if (null != stock) {
            setCache(joinKey(sid, cid), stock);
        }
        return stock;
    }

    public int total(int gid, int sid, int ctype, Date start, Date end, String search) {
        if (null != search) {
            switch (TypeDefine.CommodityType.valueOf(ctype)) {
                case COMMODITY:
                    return myStockDetailMapper.count_commodity(gid, sid, start, end, "%" + search + "%");
                case HALFGOOD:
                    return myStockDetailMapper.count_halfgood(gid, sid, start, end, "%" + search + "%");
                case ORIGINAL:
                    return myStockDetailMapper.count_original(gid, sid, start, end, "%" + search + "%");
                case STANDARD:
                    return myStockDetailMapper.count_standard(gid, sid, start, end, "%" + search + "%");
                default:
                    return 0;
            }
        } else {
            TStockDetailExample example = new TStockDetailExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(start).andCdateLessThanOrEqualTo(end);
            return (int) stockDetailMapper.countByExample(example);
        }
    }

    public List<MyStockDetail> pagination(int gid, int sid, int page, int limit, int ctype, Date start, Date end, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        switch (TypeDefine.CommodityType.valueOf(ctype)) {
            case COMMODITY:
                return myStockDetailMapper.pagination_commodity((page - 1) * limit, limit, gid, sid, start, end, key);
            case HALFGOOD:
                return myStockDetailMapper.pagination_halfgood((page - 1) * limit, limit, gid, sid, start, end, key);
            case ORIGINAL:
                return myStockDetailMapper.pagination_original((page - 1) * limit, limit, gid, sid, start, end, key);
            case STANDARD:
                return myStockDetailMapper.pagination_standard((page - 1) * limit, limit, gid, sid, start, end, key);
            default:
                return null;
        }
    }

    public boolean insert(int gid, int sid, int otype, Integer oid, int ctype, int cid, BigDecimal price, int weight, int value, Date cdate) {
        TStockDetail row = new TStockDetail();
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
        if (stockDetailMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int cid) {
        TStockDetail stock = find(sid, cid);
        if (null == stock) {
            return false;
        }
        delCache(joinKey(stock.getSid(), stock.getCid()));
        return stockDetailMapper.deleteByPrimaryKey(stock.getId()) > 0;
    }
}
