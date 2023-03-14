package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockDetailMapper;
import com.cxb.storehelperserver.model.TStockDetail;
import com.cxb.storehelperserver.model.TStockDetailExample;
import com.cxb.storehelperserver.repository.mapper.MyStockDetailMapper;
import com.cxb.storehelperserver.repository.model.MyStockDetail;
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

    public int total(int gid, int sid, String search) {
        if (null != search) {
            return myStockDetailMapper.count(gid, sid, "%" + search + "%");
        } else {
            int total = getTotalCache(joinKey(gid, sid));
            if (0 != total) {
                return total;
            }
            TStockDetailExample example = new TStockDetailExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid);
            total = (int) stockDetailMapper.countByExample(example);
            setTotalCache(joinKey(gid, sid), total);
            return total;
        }
    }

    public List<MyStockDetail> pagination(int gid, int sid, int page, int limit, String search) {
        if (null != search) {
            return myStockDetailMapper.pagination((page - 1) * limit, limit, gid, sid, "%" + search + "%");
        } else {
            return myStockDetailMapper.pagination((page - 1) * limit, limit, gid, sid, null);
        }
    }

    public boolean insert(int gid, int sid, int otype, int oid, int ctype, int cid, BigDecimal price, int weight, int value, Date cdate) {
        TStockDetail row = new TStockDetail();
        row.setGid(gid);
        row.setSid(sid);
        row.setOtype(otype);
        row.setOid(oid);
        row.setCtype(ctype);
        row.setCid(cid);
        row.setPrice(price);
        row.setWeight(weight);
        row.setValue(value);
        row.setCdate(cdate);
        if (stockDetailMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getSid()));
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
        delTotalCache(joinKey(stock.getGid(), stock.getSid()));
        return stockDetailMapper.deleteByPrimaryKey(stock.getId()) > 0;
    }
}
