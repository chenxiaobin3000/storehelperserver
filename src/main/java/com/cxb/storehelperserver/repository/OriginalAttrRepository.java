package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalAttrMapper;
import com.cxb.storehelperserver.model.TOriginalAttr;
import com.cxb.storehelperserver.model.TOriginalAttrExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 原料属性仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class OriginalAttrRepository extends BaseRepository<TOriginalAttr> {
    @Resource
    private TOriginalAttrMapper originalAttrMapper;

    private final String cacheOriName;

    public OriginalAttrRepository() {
        init("oriAttr::");
        cacheOriName = cacheName + "ori::";
    }

    public TOriginalAttr find(int id) {
        TOriginalAttr originalAttr = getCache(id, TOriginalAttr.class);
        if (null != originalAttr) {
            return originalAttr;
        }

        // 缓存没有就查询数据库
        originalAttr = originalAttrMapper.selectByPrimaryKey(id);
        if (null != originalAttr) {
            setCache(id, originalAttr);
        }
        return originalAttr;
    }

    public List<TOriginalAttr> findByOriginal(int oid) {
        List<TOriginalAttr> originalAtrrs = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheOriName + oid));
        if (null != originalAtrrs) {
            return originalAtrrs;
        }
        TOriginalAttrExample example = new TOriginalAttrExample();
        example.or().andOidEqualTo(oid);
        originalAtrrs = originalAttrMapper.selectByExample(example);
        if (null != originalAtrrs) {
            redisTemplate.opsForValue().set(cacheName + cacheOriName + oid, originalAtrrs);
        }
        return originalAtrrs;
    }

    public boolean insert(TOriginalAttr row) {
        if (originalAttrMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheOriName + row.getOid());
            return true;
        }
        return false;
    }

    public boolean update(TOriginalAttr row) {
        if (originalAttrMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheOriName + row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TOriginalAttr originalAttr = find(id);
        if (null == originalAttr) {
            return false;
        }
        delCache(cacheOriName + originalAttr.getOid());
        delCache(id);
        return originalAttrMapper.deleteByPrimaryKey(id) > 0;
    }
}
