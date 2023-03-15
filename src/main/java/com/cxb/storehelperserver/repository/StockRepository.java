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
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;

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

    public TStock find(int sid, int ctype, int cid) {
        TStock stock = getCache(joinKey(sid, ctype, cid), TStock.class);
        if (null != stock) {
            return stock;
        }

        // 缓存没有就查询数据库
        TStockExample example = new TStockExample();
        example.or().andSidEqualTo(sid).andCtypeEqualTo(ctype).andCidEqualTo(cid);
        stock = stockMapper.selectOneByExample(example);
        if (null != stock) {
            setCache(joinKey(sid, ctype, cid), stock);
        }
        return stock;
    }

    public List<MyStockReport> findReport(int gid, int sid, int ctype) {
        return myStockMapper.selectReport(gid, sid, ctype);
    }

    public int total(int gid, int sid, int ctype, String search) {
        if (null != search) {
            switch (CommodityType.valueOf(ctype)) {
                case COMMODITY:
                    return myStockMapper.count_commodity(gid, sid, "%" + search + "%");
                case HALFGOOD:
                    return myStockMapper.count_halfgood(gid, sid, "%" + search + "%");
                case ORIGINAL:
                    return myStockMapper.count_original(gid, sid, "%" + search + "%");
                case STANDARD:
                    return myStockMapper.count_standard(gid, sid, "%" + search + "%");
                default:
                    return 0;
            }
        } else {
            int total = getTotalCache(joinKey(gid, sid, ctype));
            if (0 != total) {
                return total;
            }
            TStockExample example = new TStockExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid).andCtypeEqualTo(ctype);
            total = (int) stockMapper.countByExample(example);
            setTotalCache(joinKey(gid, sid, ctype), total);
            return total;
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, int ctype, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        switch (CommodityType.valueOf(ctype)) {
            case COMMODITY:
                return myStockMapper.pagination_commodity((page - 1) * limit, limit, gid, sid, key);
            case HALFGOOD:
                return myStockMapper.pagination_halfgood((page - 1) * limit, limit, gid, sid, key);
            case ORIGINAL:
                return myStockMapper.pagination_original((page - 1) * limit, limit, gid, sid, key);
            case STANDARD:
                return myStockMapper.pagination_standard((page - 1) * limit, limit, gid, sid, key);
            default:
                return null;
        }
    }

    public boolean insert(int gid, int sid, int ctype, int cid, BigDecimal price, int weight, int value) {
        TStock row = new TStock();
        row.setGid(gid);
        row.setSid(sid);
        row.setCtype(ctype);
        row.setCid(cid);
        row.setPrice(price);
        row.setWeight(weight);
        row.setValue(value);
        if (stockMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCtype(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getSid(), row.getCtype()));
            return true;
        }
        return false;
    }

    public boolean update(TStock row) {
        if (stockMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCtype(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int ctype, int cid) {
        TStock stock = find(sid, ctype, cid);
        if (null == stock) {
            return false;
        }
        delCache(joinKey(stock.getSid(), stock.getCtype(), stock.getCid()));
        delTotalCache(joinKey(stock.getGid(), stock.getSid(), stock.getCtype()));
        return stockMapper.deleteByPrimaryKey(stock.getId()) > 0;
    }
}
