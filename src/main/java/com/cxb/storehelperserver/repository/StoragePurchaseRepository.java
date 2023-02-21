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

    public boolean insert(int pid, int sid) {
        TStoragePurchase row = new TStoragePurchase();
        row.setPid(pid);
        row.setSid(sid);
        if (storagePurchaseMapper.insert(row) > 0) {
            setCache(row.getSid(), row);
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

    public boolean delete(int id) {
        TStoragePurchase storagePurchase = storagePurchaseMapper.selectByPrimaryKey(id);
        if (null == storagePurchase) {
            return false;
        }
        delCache(storagePurchase.getSid());
        return storagePurchaseMapper.deleteByPrimaryKey(id) > 0;
    }
}
