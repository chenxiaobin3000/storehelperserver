package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStandardStorageMapper;
import com.cxb.storehelperserver.model.TStandardStorage;
import com.cxb.storehelperserver.model.TStandardStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 标品仓库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StandardStorageRepository extends BaseRepository<TStandardStorage> {
    @Resource
    private TStandardStorageMapper commodityStorageMapper;

    public StandardStorageRepository() {
        init("stanStorage::");
    }

    public TStandardStorage find(int cid) {
        TStandardStorage commodityStorage = getCache(cid, TStandardStorage.class);
        if (null != commodityStorage) {
            return commodityStorage;
        }

        // 缓存没有就查询数据库
        TStandardStorageExample example = new TStandardStorageExample();
        example.or().andCidEqualTo(cid);
        commodityStorage = commodityStorageMapper.selectOneByExample(example);
        if (null != commodityStorage) {
            setCache(cid, commodityStorage);
        }
        return commodityStorage;
    }

    public boolean insert(TStandardStorage row) {
        if (commodityStorageMapper.insert(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStandardStorage row) {
        if (commodityStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TStandardStorageExample example = new TStandardStorageExample();
        example.or().andCidEqualTo(cid);
        return commodityStorageMapper.deleteByExample(example) > 0;
    }
}
