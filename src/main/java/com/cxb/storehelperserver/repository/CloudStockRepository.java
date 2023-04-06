package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudStockMapper;
import com.cxb.storehelperserver.model.TCloudStock;
import com.cxb.storehelperserver.model.TCloudStockExample;
import com.cxb.storehelperserver.repository.mapper.MyCloudStockMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;

/**
 * desc: 云仓库存仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class CloudStockRepository extends BaseRepository<TCloudStock> {
    @Resource
    private TCloudStockMapper cloudStockMapper;

    @Resource
    private MyCloudStockMapper myCloudStockMapper;

    public CloudStockRepository() {
        init("cloudStock::");
    }

    public TCloudStock find(int sid, int ctype, int cid) {
        TCloudStock stock = getCache(joinKey(sid, ctype, cid), TCloudStock.class);
        if (null != stock) {
            return stock;
        }

        // 缓存没有就查询数据库
        TCloudStockExample example = new TCloudStockExample();
        example.or().andSidEqualTo(sid).andCtypeEqualTo(ctype).andCidEqualTo(cid);
        stock = cloudStockMapper.selectOneByExample(example);
        if (null != stock) {
            setCache(joinKey(sid, ctype, cid), stock);
        }
        return stock;
    }

    public List<MyStockReport> findReport(int gid, int sid, int ctype) {
        return myCloudStockMapper.selectReport(gid, sid, ctype);
    }

    public int total(int gid, int sid, int ctype, String search) {
        if (null != search) {
            switch (CommodityType.valueOf(ctype)) {
                case COMMODITY:
                    return myCloudStockMapper.count_commodity(gid, sid, "%" + search + "%");
                case HALFGOOD:
                    return myCloudStockMapper.count_halfgood(gid, sid, "%" + search + "%");
                case ORIGINAL:
                    return myCloudStockMapper.count_original(gid, sid, "%" + search + "%");
                case STANDARD:
                    return myCloudStockMapper.count_standard(gid, sid, "%" + search + "%");
                default:
                    return 0;
            }
        } else {
            int total = getTotalCache(joinKey(gid, sid, ctype));
            if (0 != total) {
                return total;
            }
            TCloudStockExample example = new TCloudStockExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid).andCtypeEqualTo(ctype);
            total = (int) cloudStockMapper.countByExample(example);
            setTotalCache(joinKey(gid, sid, ctype), total);
            return total;
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, int type, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        switch (CommodityType.valueOf(type)) {
            case COMMODITY:
                return myCloudStockMapper.pagination_commodity((page - 1) * limit, limit, gid, sid, key);
            case HALFGOOD:
                return myCloudStockMapper.pagination_halfgood((page - 1) * limit, limit, gid, sid, key);
            case ORIGINAL:
                return myCloudStockMapper.pagination_original((page - 1) * limit, limit, gid, sid, key);
            case STANDARD:
                return myCloudStockMapper.pagination_standard((page - 1) * limit, limit, gid, sid, key);
            default:
                return null;
        }
    }

    public List<TCloudStock> all(int sid) {
        TCloudStockExample example = new TCloudStockExample();
        example.or().andSidEqualTo(sid);
        return cloudStockMapper.selectByExample(example);
    }

    public boolean insert(int gid, int sid, int ctype, int cid, BigDecimal price, int weight, int value) {
        TCloudStock row = new TCloudStock();
        row.setGid(gid);
        row.setSid(sid);
        row.setCtype(ctype);
        row.setCid(cid);
        row.setPrice(price);
        row.setWeight(weight);
        row.setValue(value);
        if (cloudStockMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCtype(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getSid(), row.getCtype()));
            return true;
        }
        return false;
    }

    public boolean update(TCloudStock row) {
        if (cloudStockMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCtype(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int ctype, int cid) {
        TCloudStock stock = find(sid, ctype, cid);
        if (null == stock) {
            return false;
        }
        delCache(joinKey(stock.getSid(), stock.getCtype(), stock.getCid()));
        delTotalCache(joinKey(stock.getGid(), stock.getSid(), stock.getCtype()));
        return cloudStockMapper.deleteByPrimaryKey(stock.getId()) > 0;
    }
}
