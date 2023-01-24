package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalCommodityMapper;
import com.cxb.storehelperserver.model.TOriginalCommodity;
import com.cxb.storehelperserver.model.TOriginalCommodityExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料商品仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OriginalCommodityRepository extends BaseRepository<TOriginalCommodity> {
    @Resource
    private TOriginalCommodityMapper originalCommodityMapper;

    public OriginalCommodityRepository() {
        init("oriComm::");
    }

    public TOriginalCommodity find(int gid, int cid) {
        TOriginalCommodity originalCommodity = getCache(joinKey(gid, cid), TOriginalCommodity.class);
        if (null != originalCommodity) {
            return originalCommodity;
        }

        // 缓存没有就查询数据库
        TOriginalCommodityExample example = new TOriginalCommodityExample();
        example.or().andGidEqualTo(gid).andCidEqualTo(cid);
        originalCommodity = originalCommodityMapper.selectOneByExample(example);
        if (null != originalCommodity) {
            setCache(joinKey(gid, cid), originalCommodity);
        }
        return originalCommodity;
    }

    public boolean insert(TOriginalCommodity row) {
        if (originalCommodityMapper.insert(row) > 0) {
            setCache(joinKey(row.getGid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean update(TOriginalCommodity row) {
        if (originalCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getGid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int gid, int cid) {
        delCache(joinKey(gid, cid));
        TOriginalCommodityExample example = new TOriginalCommodityExample();
        example.or().andGidEqualTo(gid).andCidEqualTo(cid);
        return originalCommodityMapper.deleteByExample(example) > 0;
    }
}
