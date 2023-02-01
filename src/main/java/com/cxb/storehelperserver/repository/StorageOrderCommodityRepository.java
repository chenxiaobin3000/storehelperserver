package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageOrderCommodityMapper;
import com.cxb.storehelperserver.model.TStorageOrderCommodity;
import com.cxb.storehelperserver.model.TStorageOrderCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageOrderCommodityMapper;
import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 进货出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class StorageOrderCommodityRepository extends BaseRepository<List> {
    @Resource
    private TStorageOrderCommodityMapper storageOrderCommodityMapper;

    @Resource
    private MyStorageOrderCommodityMapper myStorageOrderCommodityMapper;

    public StorageOrderCommodityRepository() {
        init("soComm::");
    }

    public List<TStorageOrderCommodity> find(int oid) {
        List<TStorageOrderCommodity> storageOrderCommoditys = getCache(oid, List.class);
        if (null != storageOrderCommoditys) {
            return storageOrderCommoditys;
        }

        // 缓存没有就查询数据库
        TStorageOrderCommodityExample example = new TStorageOrderCommodityExample();
        example.or().andOidEqualTo(oid);
        storageOrderCommoditys = storageOrderCommodityMapper.selectByExample(example);
        if (null != storageOrderCommoditys) {
            setCache(oid, storageOrderCommoditys);
        }
        return storageOrderCommoditys;
    }

    public List<MyOrderCommodity> findBySid(int sid, Date start, Date end) {
        return myStorageOrderCommodityMapper.select(sid, start, end);
    }

    // 注意：数据被缓存在StorageCommodityService，所以不能直接调用该函数
    public boolean update(List<TStorageOrderCommodity> rows, int oid) {
        delete(oid);
        for (TStorageOrderCommodity storageOrderCommodity : rows) {
            if (storageOrderCommodityMapper.insert(storageOrderCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TStorageOrderCommodityExample example = new TStorageOrderCommodityExample();
        example.or().andOidEqualTo(oid);
        return storageOrderCommodityMapper.deleteByExample(example) > 0;
    }
}
