package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalDestroyMapper;
import com.cxb.storehelperserver.model.TOriginalDestroy;
import com.cxb.storehelperserver.model.TOriginalDestroyExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料废料仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OriginalDestroyRepository extends BaseRepository<TOriginalDestroy> {
    @Resource
    private TOriginalDestroyMapper originalDestroyMapper;

    public OriginalDestroyRepository() {
        init("oriDest::");
    }

    public TOriginalDestroy find(int gid, int did) {
        TOriginalDestroy originalDestroy = getCache(getKey(gid, did), TOriginalDestroy.class);
        if (null != originalDestroy) {
            return originalDestroy;
        }

        // 缓存没有就查询数据库
        TOriginalDestroyExample example = new TOriginalDestroyExample();
        example.or().andGidEqualTo(gid).andDidEqualTo(did);
        originalDestroy = originalDestroyMapper.selectOneByExample(example);
        if (null != originalDestroy) {
            setCache(getKey(gid, did), originalDestroy);
        }
        return originalDestroy;
    }

    public boolean insert(TOriginalDestroy row) {
        if (originalDestroyMapper.insert(row) > 0) {
            setCache(getKey(row.getGid(), row.getDid()), row);
            return true;
        }
        return false;
    }

    public boolean update(TOriginalDestroy row) {
        if (originalDestroyMapper.updateByPrimaryKey(row) > 0) {
            setCache(getKey(row.getGid(), row.getDid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int gid, int did) {
        delCache(getKey(gid, did));
        TOriginalDestroyExample example = new TOriginalDestroyExample();
        example.or().andGidEqualTo(gid).andDidEqualTo(did);
        return originalDestroyMapper.deleteByExample(example) > 0;
    }

    private String getKey(int gid, int did) {
        return String.valueOf(gid) + "::" + String.valueOf(did);
    }
}
