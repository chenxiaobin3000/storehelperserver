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
import java.util.Date;
import java.util.List;

/**
 * desc: 库存仓库
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

    public TStock find(int sid, int cid) {
        TStock stock = getCache(joinKey(sid, cid), TStock.class);
        if (null != stock) {
            return stock;
        }

        // 缓存没有就查询数据库
        TStockExample example = new TStockExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(cid);
        stock = stockMapper.selectOneByExample(example);
        if (null != stock) {
            setCache(joinKey(sid, cid), stock);
        }
        return stock;
    }

    public List<MyStockReport> findReport(int gid, int sid) {
        return myStockMapper.selectReport(gid, sid);
    }

    public int total(int gid, int sid, String search) {
        if (null != search) {
            return myStockMapper.count(gid, sid, "%" + search + "%");
        } else {
            int total = getTotalCache(joinKey(gid, sid));
            if (0 != total) {
                return total;
            }
            TStockExample example = new TStockExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid);
            total = (int) stockMapper.countByExample(example);
            setTotalCache(joinKey(gid, sid), total);
            return total;
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockMapper.pagination((page - 1) * limit, limit, gid, sid, "%" + search + "%");
        } else {
            return myStockMapper.pagination((page - 1) * limit, limit, gid, sid, null);
        }
    }

    public boolean insert(TStock row) {
        if (stockMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getSid()));
            return true;
        }
        return false;
    }

    public boolean update(TStock row) {
        if (stockMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int cid) {
        TStock stock = find(sid, cid);
        if (null == stock) {
            return false;
        }
        delCache(joinKey(stock.getSid(), stock.getCid()));
        delTotalCache(joinKey(stock.getGid(), stock.getSid()));
        return stockMapper.deleteByPrimaryKey(stock.getId()) > 0;
    }
}
