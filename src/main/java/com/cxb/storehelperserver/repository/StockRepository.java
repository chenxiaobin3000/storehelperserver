package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockMapper;
import com.cxb.storehelperserver.model.TStock;
import com.cxb.storehelperserver.model.TStockExample;
import com.cxb.storehelperserver.repository.mapper.MyStockMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.*;

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

    public int total(int gid, int sid, CommodityType type, String search) {
        if (null != search) {
            switch (type) {
                case COMMODITY:
                    return myStockMapper.count_commodity(gid, sid, "%" + search + "%");
            }
            return 0;
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

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, CommodityType type, Date date, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        switch (type) {
            case COMMODITY:
                return myStockMapper.pagination_commodity((page - 1) * limit, limit, gid, sid, key);
        }
        return null;
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
