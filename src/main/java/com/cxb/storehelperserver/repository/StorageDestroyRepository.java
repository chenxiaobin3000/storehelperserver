package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageDestroyMapper;
import com.cxb.storehelperserver.model.TStorageDestroy;
import com.cxb.storehelperserver.model.TStorageDestroyExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageDestroyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储废料仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StorageDestroyRepository extends BaseRepository<TStorageDestroy> {
    @Resource
    private TStorageDestroyMapper storageDestroyMapper;

    @Resource
    private MyStorageDestroyMapper myStorageDestroyMapper;

    public StorageDestroyRepository() {
        init("storageDest::");
    }

    public TStorageDestroy find(int id) {
        TStorageDestroy storageDestroy = getCache(id, TStorageDestroy.class);
        if (null != storageDestroy) {
            return storageDestroy;
        }

        // 缓存没有就查询数据库
        storageDestroy = storageDestroyMapper.selectByPrimaryKey(id);
        if (null != storageDestroy) {
            setCache(id, storageDestroy);
        }
        return storageDestroy;
    }

    public int total(int sid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            return myStorageDestroyMapper.countByExample(sid, "%" + search + "%");
        } else {
            int total = getTotalCache(sid);
            if (0 != total) {
                return total;
            }

            TStorageDestroyExample example = new TStorageDestroyExample();
            example.or().andSidEqualTo(sid);
            total = (int) storageDestroyMapper.countByExample(example);
            setTotalCache(sid, total);
            return total;
        }
    }

    public List<TStorageDestroy> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageDestroyMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageDestroyMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageDestroy row) {
        if (storageDestroyMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getSid());
            return true;
        }
        return false;
    }

    public boolean update(TStorageDestroy row) {
        if (storageDestroyMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageDestroy storageDestroy = find(id);
        if (null == storageDestroy) {
            return false;
        }
        delCache(id);
        delTotalCache(storageDestroy.getSid());
        return storageDestroyMapper.deleteByPrimaryKey(id) > 0;
    }
}
