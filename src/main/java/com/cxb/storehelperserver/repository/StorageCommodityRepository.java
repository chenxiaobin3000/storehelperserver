package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageCommodityMapper;
import com.cxb.storehelperserver.model.TStorageCommodity;
import com.cxb.storehelperserver.model.TStorageCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageCommodityMapper;
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
public class StorageCommodityRepository extends BaseRepository<List> {
    @Resource
    private TStorageCommodityMapper storageCommodityMapper;

    @Resource
    private MyStorageCommodityMapper myStorageCommodityMapper;

    public StorageCommodityRepository() {
        init("storageComm::");
    }

    public List<TStorageCommodity> find(int oid) {
        List<TStorageCommodity> storageCommoditys = getCache(oid, List.class);
        if (null != storageCommoditys) {
            return storageCommoditys;
        }

        // 缓存没有就查询数据库
        TStorageCommodityExample example = new TStorageCommodityExample();
        example.or().andOidEqualTo(oid);
        storageCommoditys = storageCommodityMapper.selectByExample(example);
        if (null != storageCommoditys) {
            setCache(oid, storageCommoditys);
        }
        return storageCommoditys;
    }

    public List<MyOrderCommodity> findByGid(int gid, Date start, Date end) {
        return myStorageCommodityMapper.selectByGid(gid, start, end);
    }

    public List<MyOrderCommodity> findBySid(int sid, Date start, Date end) {
        return myStorageCommodityMapper.selectBySid(sid, start, end);
    }

    // 注意：数据被缓存在StorageCommodityService，所以不能直接调用该函数
    public boolean update(List<TStorageCommodity> rows, int oid) {
        delete(oid);
        for (TStorageCommodity storageCommodity : rows) {
            if (storageCommodityMapper.insert(storageCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TStorageCommodityExample example = new TStorageCommodityExample();
        example.or().andOidEqualTo(oid);
        return storageCommodityMapper.deleteByExample(example) > 0;
    }
}