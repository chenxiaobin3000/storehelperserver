package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.THalfgoodStorageMapper;
import com.cxb.storehelperserver.model.THalfgoodStorage;
import com.cxb.storehelperserver.model.THalfgoodStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 半成品仓库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class HalfgoodStorageRepository extends BaseRepository<THalfgoodStorage> {
    @Resource
    private THalfgoodStorageMapper commodityStorageMapper;

    public HalfgoodStorageRepository() {
        init("halfStorage::");
    }

    public THalfgoodStorage find(int cid) {
        THalfgoodStorage commodityStorage = getCache(cid, THalfgoodStorage.class);
        if (null != commodityStorage) {
            return commodityStorage;
        }

        // 缓存没有就查询数据库
        THalfgoodStorageExample example = new THalfgoodStorageExample();
        example.or().andCidEqualTo(cid);
        commodityStorage = commodityStorageMapper.selectOneByExample(example);
        if (null != commodityStorage) {
            setCache(cid, commodityStorage);
        }
        return commodityStorage;
    }

    public boolean insert(THalfgoodStorage row) {
        if (commodityStorageMapper.insert(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean update(THalfgoodStorage row) {
        if (commodityStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        THalfgoodStorageExample example = new THalfgoodStorageExample();
        example.or().andCidEqualTo(cid);
        return commodityStorageMapper.deleteByExample(example) > 0;
    }
}
