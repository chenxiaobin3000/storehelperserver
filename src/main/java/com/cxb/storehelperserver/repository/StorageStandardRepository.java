package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageStandardMapper;
import com.cxb.storehelperserver.model.TStorageStandard;
import com.cxb.storehelperserver.model.TStorageStandardExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageStandardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储标品仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StorageStandardRepository extends BaseRepository<TStorageStandard> {
    @Resource
    private TStorageStandardMapper storageStandardMapper;

    @Resource
    private MyStorageStandardMapper myStorageStandardMapper;

    public StorageStandardRepository() {
        init("storageStan::");
    }

    public TStorageStandard find(int id) {
        TStorageStandard storageStandard = getCache(id, TStorageStandard.class);
        if (null != storageStandard) {
            return storageStandard;
        }

        // 缓存没有就查询数据库
        storageStandard = storageStandardMapper.selectByPrimaryKey(id);
        if (null != storageStandard) {
            setCache(id, storageStandard);
        }
        return storageStandard;
    }

    public int total(int sid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            return myStorageStandardMapper.countByExample(sid, "%" + search + "%");
        } else {
            int total = getTotalCache(sid);
            if (0 != total) {
                return total;
            }

            TStorageStandardExample example = new TStorageStandardExample();
            example.or().andSidEqualTo(sid);
            total = (int) storageStandardMapper.countByExample(example);
            setTotalCache(sid, total);
            return total;
        }
    }

    public List<TStorageStandard> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageStandardMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageStandardMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageStandard row) {
        if (storageStandardMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getSid());
            return true;
        }
        return false;
    }

    public boolean update(TStorageStandard row) {
        if (storageStandardMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageStandard storageStandard = find(id);
        if (null == storageStandard) {
            return false;
        }
        delCache(id);
        delTotalCache(storageStandard.getSid());
        return storageStandardMapper.deleteByPrimaryKey(id) > 0;
    }
}
