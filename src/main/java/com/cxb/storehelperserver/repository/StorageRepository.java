package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageMapper;
import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.model.TStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class StorageRepository extends BaseRepository<TStorage> {
    @Resource
    private TStorageMapper storageMapper;

    public StorageRepository() {
        init("storage::");
    }

    public TStorage find(int id) {
        TStorage storage = getCache(id, TStorage.class);
        if (null != storage) {
            return storage;
        }

        // 缓存没有就查询数据库
        storage = storageMapper.selectByPrimaryKey(id);
        if (null != storage) {
            setCache(id, storage);
        }
        return storage;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TStorageExample example = new TStorageExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) storageMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TStorageExample example = new TStorageExample();
            example.or().andGidEqualTo(gid);
            total = (int) storageMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TStorage> pagination(int gid, int page, int limit, String search) {
        TStorageExample example = new TStorageExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return storageMapper.selectByExample(example);
    }

    /*
     * desc: 判断公司是否存在仓库
     */
    public boolean check(int gid, String name, int id) {
        TStorageExample example = new TStorageExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != storageMapper.selectOneByExample(example);
        } else {
            TStorage storage = storageMapper.selectOneByExample(example);
            return null != storage && !storage.getId().equals(id);
        }
    }

    public boolean insert(TStorage row) {
        if (storageMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TStorage row) {
        if (storageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorage storage = find(id);
        if (null == storage) {
            return false;
        }
        delCache(id);
        delTotalCache(storage.getGid());
        return storageMapper.deleteByPrimaryKey(id) > 0;
    }
}
