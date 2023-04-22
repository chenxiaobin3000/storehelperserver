package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageTypeMapper;
import com.cxb.storehelperserver.model.TStorageType;
import com.cxb.storehelperserver.model.TStorageTypeExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储类型仓库
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Repository
public class StorageTypeRepository extends BaseRepository<TStorageType> {
    @Resource
    private TStorageTypeMapper storageTypeMapper;

    private final String cacheGroupName;

    public StorageTypeRepository() {
        init("storageType::");
        cacheGroupName = cacheName + "group::";
    }

    public TStorageType find(int id) {
        TStorageType storageType = getCache(id, TStorageType.class);
        if (null != storageType) {
            return storageType;
        }

        // 缓存没有就查询数据库
        storageType = storageTypeMapper.selectByPrimaryKey(id);
        if (null != storageType) {
            setCache(id, storageType);
        }
        return storageType;
    }

    public List<TStorageType> findByGroup(int gid) {
        List<TStorageType> types = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != types) {
            return types;
        }
        TStorageTypeExample example = new TStorageTypeExample();
        example.or().andGidEqualTo(gid);
        types = storageTypeMapper.selectByExample(example);
        if (null != types) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, types);
        }
        return types;
    }

    public boolean insert(TStorageType row) {
        if (storageTypeMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TStorageType row) {
        if (storageTypeMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageType storageType = find(id);
        if (null == storageType) {
            return false;
        }
        delCache(cacheGroupName + storageType.getGid());
        delCache(id);
        return storageTypeMapper.deleteByPrimaryKey(id) > 0;
    }
}
