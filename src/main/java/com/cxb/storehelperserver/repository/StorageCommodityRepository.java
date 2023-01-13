package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageCommodityMapper;
import com.cxb.storehelperserver.model.TStorageCommodity;
import com.cxb.storehelperserver.model.TStorageCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageCommodityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储商品仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StorageCommodityRepository extends BaseRepository<TStorageCommodity> {
    @Resource
    private TStorageCommodityMapper storageCommodityMapper;

    @Resource
    private MyStorageCommodityMapper myStorageCommodityMapper;

    public StorageCommodityRepository() {
        init("storageComm::");
    }

    public TStorageCommodity find(int id) {
        TStorageCommodity storageCommodity = getCache(id, TStorageCommodity.class);
        if (null != storageCommodity) {
            return storageCommodity;
        }

        // 缓存没有就查询数据库
        storageCommodity = storageCommodityMapper.selectByPrimaryKey(id);
        if (null != storageCommodity) {
            setCache(id, storageCommodity);
        }
        return storageCommodity;
    }

    public int total(int sid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            return myStorageCommodityMapper.countByExample(sid, "%" + search + "%");
        } else {
            int total = getTotalCache(sid);
            if (0 != total) {
                return total;
            }

            TStorageCommodityExample example = new TStorageCommodityExample();
            example.or().andSidEqualTo(sid);
            total = (int) storageCommodityMapper.countByExample(example);
            setTotalCache(sid, total);
            return total;
        }
    }

    public List<TStorageCommodity> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageCommodityMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageCommodityMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageCommodity row) {
        if (storageCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getSid());
            return true;
        }
        return false;
    }

    public boolean update(TStorageCommodity row) {
        if (storageCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageCommodity storageCommodity = find(id);
        if (null == storageCommodity) {
            return false;
        }
        delCache(id);
        delTotalCache(storageCommodity.getSid());
        return storageCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
