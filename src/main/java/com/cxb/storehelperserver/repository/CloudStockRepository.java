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
import java.util.List;

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

    public TCloudStock find(int sid, int cid) {
        TCloudStock stock = getCache(joinKey(sid, cid), TCloudStock.class);
        if (null != stock) {
            return stock;
        }

        // 缓存没有就查询数据库
        TCloudStockExample example = new TCloudStockExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(cid);
        stock = cloudStockMapper.selectOneByExample(example);
        if (null != stock) {
            setCache(joinKey(sid, cid), stock);
        }
        return stock;
    }

    public List<MyStockReport> findReport(int gid, int sid) {
        return myCloudStockMapper.selectReport(gid, sid);
    }

    public int total(int gid, int sid, String search) {
        if (null != search) {
            return myCloudStockMapper.count(gid, sid, "%" + search + "%");
        } else {
            int total = getTotalCache(joinKey(gid, sid));
            if (0 != total) {
                return total;
            }
            TCloudStockExample example = new TCloudStockExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid);
            total = (int) cloudStockMapper.countByExample(example);
            setTotalCache(joinKey(gid, sid), total);
            return total;
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, String search) {
        if (null != search) {
            return myCloudStockMapper.pagination((page - 1) * limit, limit, gid, sid, "%" + search + "%");
        } else {
            return myCloudStockMapper.pagination((page - 1) * limit, limit, gid, sid, null);
        }
    }

    public boolean insert(TCloudStock row) {
        if (cloudStockMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getSid()));
            return true;
        }
        return false;
    }

    public boolean update(TCloudStock row) {
        if (cloudStockMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int cid) {
        TCloudStock stock = find(sid, cid);
        if (null == stock) {
            return false;
        }
        delCache(joinKey(stock.getSid(), stock.getCid()));
        delTotalCache(joinKey(stock.getGid(), stock.getSid()));
        return cloudStockMapper.deleteByPrimaryKey(stock.getId()) > 0;
    }
}
