package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalMapper;
import com.cxb.storehelperserver.model.TOriginal;
import com.cxb.storehelperserver.model.TOriginalExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 原料仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class OriginalRepository extends BaseRepository<TOriginal> {
    @Resource
    private TOriginalMapper originalMapper;

    private final String cacheGroupName;

    public OriginalRepository() {
        init("ori::");
        cacheGroupName = cacheName + "group::";
    }

    public TOriginal find(int id) {
        TOriginal original = getCache(id, TOriginal.class);
        if (null != original) {
            return original;
        }

        // 缓存没有就查询数据库
        original = originalMapper.selectByPrimaryKey(id);
        if (null != original) {
            setCache(id, original);
        }
        return original;
    }

    public List<TOriginal> findByGroup(int gid) {
        List<TOriginal> originals = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != originals) {
            return originals;
        }
        TOriginalExample example = new TOriginalExample();
        example.or().andGidEqualTo(gid);
        originals = originalMapper.selectByExample(example);
        if (null != originals) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, originals);
        }
        return originals;
    }

    public boolean insert(TOriginal row) {
        if (originalMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TOriginal row) {
        if (originalMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TOriginal original = find(id);
        if (null == original) {
            return false;
        }
        delCache(cacheGroupName + original.getGid());
        delCache(id);
        return originalMapper.deleteByPrimaryKey(id) > 0;
    }
}
