package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageHalfgoodMapper;
import com.cxb.storehelperserver.model.TStorageHalfgood;
import com.cxb.storehelperserver.model.TStorageHalfgoodExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageHalfgoodMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储半成品仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class StorageHalfgoodRepository extends BaseRepository<TStorageHalfgood> {
    @Resource
    private TStorageHalfgoodMapper storageHalfgoodMapper;

    @Resource
    private MyStorageHalfgoodMapper myStorageHalfgoodMapper;

    public StorageHalfgoodRepository() {
        init("storageHalf::");
    }

    public TStorageHalfgood find(int id) {
        TStorageHalfgood storageHalfgood = getCache(id, TStorageHalfgood.class);
        if (null != storageHalfgood) {
            return storageHalfgood;
        }

        // 缓存没有就查询数据库
        storageHalfgood = storageHalfgoodMapper.selectByPrimaryKey(id);
        if (null != storageHalfgood) {
            setCache(id, storageHalfgood);
        }
        return storageHalfgood;
    }

    public int total(int sid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            return myStorageHalfgoodMapper.countByExample(sid, "%" + search + "%");
        } else {
            int total = getTotalCache(sid);
            if (0 != total) {
                return total;
            }

            TStorageHalfgoodExample example = new TStorageHalfgoodExample();
            example.or().andSidEqualTo(sid);
            total = (int) storageHalfgoodMapper.countByExample(example);
            setTotalCache(sid, total);
            return total;
        }
    }

    public List<TStorageHalfgood> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageHalfgoodMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageHalfgoodMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageHalfgood row) {
        if (storageHalfgoodMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getSid());
            return true;
        }
        return false;
    }

    public boolean update(TStorageHalfgood row) {
        if (storageHalfgoodMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageHalfgood storageHalfgood = find(id);
        if (null == storageHalfgood) {
            return false;
        }
        delCache(id);
        delTotalCache(storageHalfgood.getSid());
        return storageHalfgoodMapper.deleteByPrimaryKey(id) > 0;
    }
}
