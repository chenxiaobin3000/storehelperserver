package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityStorageMapper;
import com.cxb.storehelperserver.model.TCommodityStorage;
import com.cxb.storehelperserver.model.TCommodityStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 商品仓库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CommodityStorageRepository extends BaseRepository<TCommodityStorage> {
    @Resource
    private TCommodityStorageMapper commodityStorageMapper;

    public CommodityStorageRepository() {
        init("commStorage::");
    }

    public TCommodityStorage find(int cid) {
        TCommodityStorage commodityStorage = getCache(cid, TCommodityStorage.class);
        if (null != commodityStorage) {
            return commodityStorage;
        }

        // 缓存没有就查询数据库
        TCommodityStorageExample example = new TCommodityStorageExample();
        example.or().andCidEqualTo(cid);
        commodityStorage = commodityStorageMapper.selectOneByExample(example);
        if (null != commodityStorage) {
            setCache(cid, commodityStorage);
        }
        return commodityStorage;
    }

    public boolean insert(TCommodityStorage row) {
        if (commodityStorageMapper.insert(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TCommodityStorage row) {
        if (commodityStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TCommodityStorageExample example = new TCommodityStorageExample();
        example.or().andCidEqualTo(cid);
        return commodityStorageMapper.deleteByExample(example) > 0;
    }
}
