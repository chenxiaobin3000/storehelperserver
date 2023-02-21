package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudDetailMapper;
import com.cxb.storehelperserver.model.TCloudDetail;
import com.cxb.storehelperserver.model.TCloudDetailExample;
import com.cxb.storehelperserver.repository.mapper.MyCloudDetailMapper;
import com.cxb.storehelperserver.repository.model.MyStockDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 云仓库存明细仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class CloudDetailRepository extends BaseRepository<TCloudDetail> {
    @Resource
    private TCloudDetailMapper cloudDetailMapper;

    @Resource
    private MyCloudDetailMapper myCloudDetailMapper;

    public CloudDetailRepository() {
        init("cloudDetail::");
    }

    public TCloudDetail find(int sid, int cid) {
        TCloudDetail stock = getCache(joinKey(sid, cid), TCloudDetail.class);
        if (null != stock) {
            return stock;
        }

        // 缓存没有就查询数据库
        TCloudDetailExample example = new TCloudDetailExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(cid);
        stock = cloudDetailMapper.selectOneByExample(example);
        if (null != stock) {
            setCache(joinKey(sid, cid), stock);
        }
        return stock;
    }

    public int total(int gid, int sid, String search) {
        if (null != search) {
            return myCloudDetailMapper.count(gid, sid, "%" + search + "%");
        } else {
            int total = getTotalCache(joinKey(gid, sid));
            if (0 != total) {
                return total;
            }
            TCloudDetailExample example = new TCloudDetailExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid);
            total = (int) cloudDetailMapper.countByExample(example);
            setTotalCache(joinKey(gid, sid), total);
            return total;
        }
    }

    public List<MyStockDetail> pagination(int gid, int sid, int page, int limit, String search) {
        if (null != search) {
            return myCloudDetailMapper.pagination((page - 1) * limit, limit, gid, sid, "%" + search + "%");
        } else {
            return myCloudDetailMapper.pagination((page - 1) * limit, limit, gid, sid, null);
        }
    }

    public boolean insert(TCloudDetail row) {
        if (cloudDetailMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getSid()));
            return true;
        }
        return false;
    }

    public boolean update(TCloudDetail row) {
        if (cloudDetailMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int cid) {
        TCloudDetail stock = find(sid, cid);
        if (null == stock) {
            return false;
        }
        delCache(joinKey(stock.getSid(), stock.getCid()));
        delTotalCache(joinKey(stock.getGid(), stock.getSid()));
        return cloudDetailMapper.deleteByPrimaryKey(stock.getId()) > 0;
    }
}
