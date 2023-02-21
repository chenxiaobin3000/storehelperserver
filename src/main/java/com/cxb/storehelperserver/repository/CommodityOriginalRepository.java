package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityOriginalMapper;
import com.cxb.storehelperserver.model.TCommodityOriginal;
import com.cxb.storehelperserver.model.TCommodityOriginalExample;
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
public class CommodityOriginalRepository extends BaseRepository<TCommodityOriginal> {
    @Resource
    private TCommodityOriginalMapper commodityOriginalMapper;

    public CommodityOriginalRepository() {
        init("commOri::");
    }

    public TCommodityOriginal find(int gid, int cid) {
        TCommodityOriginal commodityOriginal = getCache(joinKey(gid, cid), TCommodityOriginal.class);
        if (null != commodityOriginal) {
            return commodityOriginal;
        }

        // 缓存没有就查询数据库
        TCommodityOriginalExample example = new TCommodityOriginalExample();
        example.or().andGidEqualTo(gid).andCidEqualTo(cid);
        commodityOriginal = commodityOriginalMapper.selectOneByExample(example);
        if (null != commodityOriginal) {
            setCache(joinKey(gid, cid), commodityOriginal);
        }
        return commodityOriginal;
    }

    public boolean insert(TCommodityOriginal row) {
        if (commodityOriginalMapper.insert(row) > 0) {
            setCache(joinKey(row.getGid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean update(TCommodityOriginal row) {
        if (commodityOriginalMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getGid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int gid, int cid) {
        delCache(joinKey(gid, cid));
        TCommodityOriginalExample example = new TCommodityOriginalExample();
        example.or().andGidEqualTo(gid).andCidEqualTo(cid);
        return commodityOriginalMapper.deleteByExample(example) > 0;
    }
}