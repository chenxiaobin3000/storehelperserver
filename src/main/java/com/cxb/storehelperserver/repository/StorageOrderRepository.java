package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageOrderMapper;
import com.cxb.storehelperserver.model.TStorageOrder;
import com.cxb.storehelperserver.model.TStorageOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 进货出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class StorageOrderRepository extends BaseRepository<TStorageOrder> {
    @Resource
    private TStorageOrderMapper storageOrderMapper;

    public StorageOrderRepository() {
        init("sOrder::");
    }

    public TStorageOrder find(int id) {
        TStorageOrder storageOrder = getCache(id, TStorageOrder.class);
        if (null != storageOrder) {
            return storageOrder;
        }

        // 缓存没有就查询数据库
        storageOrder = storageOrderMapper.selectByPrimaryKey(id);
        if (null != storageOrder) {
            setCache(id, storageOrder);
        }
        return storageOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TStorageOrderExample example = new TStorageOrderExample();
        example.or().andGidEqualTo(gid);
        return null != storageOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TStorageOrder row) {
        if (storageOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageOrder row) {
        if (storageOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return storageOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
