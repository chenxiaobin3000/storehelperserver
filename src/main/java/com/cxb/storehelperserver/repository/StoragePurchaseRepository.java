package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStoragePurchaseMapper;
import com.cxb.storehelperserver.model.TStoragePurchase;
import com.cxb.storehelperserver.model.TStoragePurchaseExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 入库与采购关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StoragePurchaseRepository extends BaseRepository<TStoragePurchase> {
    @Resource
    private TStoragePurchaseMapper storagePurchaseMapper;

    public StoragePurchaseRepository() {
        init("storPurchase::");
    }

    public TStoragePurchase find(int sid) {
        TStoragePurchase storagePurchase = getCache(sid, TStoragePurchase.class);
        if (null != storagePurchase) {
            return storagePurchase;
        }

        // 缓存没有就查询数据库
        TStoragePurchaseExample example = new TStoragePurchaseExample();
        example.or().andSidEqualTo(sid);
        storagePurchase = storagePurchaseMapper.selectOneByExample(example);
        if (null != storagePurchase) {
            setCache(sid, storagePurchase);
        }
        return storagePurchase;
    }

    public List<TStoragePurchase> findByPid(int pid) {
        TStoragePurchaseExample example = new TStoragePurchaseExample();
        example.or().andPidEqualTo(pid);
        return storagePurchaseMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TStoragePurchaseExample example = new TStoragePurchaseExample();
        example.or().andPidEqualTo(pid);
        return null != storagePurchaseMapper.selectOneByExample(example);
    }

    public boolean insert(int sid, int pid) {
        TStoragePurchase row = new TStoragePurchase();
        row.setSid(sid);
        row.setPid(pid);
        if (storagePurchaseMapper.insert(row) > 0) {
            setCache(sid, row);
            return true;
        }
        return false;
    }

    public boolean update(TStoragePurchase row) {
        if (storagePurchaseMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getSid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int pid) {
        delCache(sid);
        TStoragePurchaseExample example = new TStoragePurchaseExample();
        example.or().andSidEqualTo(sid).andPidEqualTo(pid);
        return storagePurchaseMapper.deleteByExample(example) > 0;
    }
}
