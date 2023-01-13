package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageOriginalMapper;
import com.cxb.storehelperserver.model.TStorageOriginal;
import com.cxb.storehelperserver.model.TStorageOriginalExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageOriginalMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储原料仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StorageOriginalRepository extends BaseRepository<TStorageOriginal> {
    @Resource
    private TStorageOriginalMapper storageOriginalMapper;

    @Resource
    private MyStorageOriginalMapper myStorageOriginalMapper;

    public StorageOriginalRepository() {
        init("storageOri::");
    }

    public TStorageOriginal find(int id) {
        TStorageOriginal storageOriginal = getCache(id, TStorageOriginal.class);
        if (null != storageOriginal) {
            return storageOriginal;
        }

        // 缓存没有就查询数据库
        storageOriginal = storageOriginalMapper.selectByPrimaryKey(id);
        if (null != storageOriginal) {
            setCache(id, storageOriginal);
        }
        return storageOriginal;
    }

    public int total(int sid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            return myStorageOriginalMapper.countByExample(sid, "%" + search + "%");
        } else {
            int total = getTotalCache(sid);
            if (0 != total) {
                return total;
            }

            TStorageOriginalExample example = new TStorageOriginalExample();
            example.or().andSidEqualTo(sid);
            total = (int) storageOriginalMapper.countByExample(example);
            setTotalCache(sid, total);
            return total;
        }
    }

    public List<TStorageOriginal> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageOriginalMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageOriginalMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageOriginal row) {
        if (storageOriginalMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getSid());
            return true;
        }
        return false;
    }

    public boolean update(TStorageOriginal row) {
        if (storageOriginalMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageOriginal storageOriginal = find(id);
        if (null == storageOriginal) {
            return false;
        }
        delCache(id);
        delTotalCache(storageOriginal.getSid());
        return storageOriginalMapper.deleteByPrimaryKey(id) > 0;
    }
}
