package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalStorageMapper;
import com.cxb.storehelperserver.model.TOriginalStorage;
import com.cxb.storehelperserver.model.TOriginalStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 废料仓库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OriginalStorageRepository extends BaseRepository<TOriginalStorage> {
    @Resource
    private TOriginalStorageMapper commodityStorageMapper;

    public OriginalStorageRepository() {
        init("oriStorage::");
    }

    public TOriginalStorage find(int cid) {
        TOriginalStorage commodityStorage = getCache(cid, TOriginalStorage.class);
        if (null != commodityStorage) {
            return commodityStorage;
        }

        // 缓存没有就查询数据库
        TOriginalStorageExample example = new TOriginalStorageExample();
        example.or().andCidEqualTo(cid);
        commodityStorage = commodityStorageMapper.selectOneByExample(example);
        if (null != commodityStorage) {
            setCache(cid, commodityStorage);
        }
        return commodityStorage;
    }

    public boolean insert(TOriginalStorage row) {
        if (commodityStorageMapper.insert(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TOriginalStorage row) {
        if (commodityStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TOriginalStorageExample example = new TOriginalStorageExample();
        example.or().andCidEqualTo(cid);
        return commodityStorageMapper.deleteByExample(example) > 0;
    }
}
