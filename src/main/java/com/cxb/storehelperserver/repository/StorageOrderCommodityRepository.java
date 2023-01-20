package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageOrderCommodityMapper;
import com.cxb.storehelperserver.model.TStorageOrderCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 进货出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class StorageOrderCommodityRepository extends BaseRepository<TStorageOrderCommodity> {
    @Resource
    private TStorageOrderCommodityMapper storageOrderCommodityMapper;

    public StorageOrderCommodityRepository() {
        init("soComm::");
    }

    public TStorageOrderCommodity find(int id) {
        TStorageOrderCommodity storageOrderCommodity = getCache(id, TStorageOrderCommodity.class);
        if (null != storageOrderCommodity) {
            return storageOrderCommodity;
        }

        // 缓存没有就查询数据库
        storageOrderCommodity = storageOrderCommodityMapper.selectByPrimaryKey(id);
        if (null != storageOrderCommodity) {
            setCache(id, storageOrderCommodity);
        }
        return storageOrderCommodity;
    }

    public boolean insert(TStorageOrderCommodity row) {
        if (storageOrderCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageOrderCommodity row) {
        if (storageOrderCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return storageOrderCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
