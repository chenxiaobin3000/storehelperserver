package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseStorageMapper;
import com.cxb.storehelperserver.model.TPurchaseStorage;
import com.cxb.storehelperserver.model.TPurchaseStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 采购入库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class PurchaseStorageRepository extends BaseRepository<TPurchaseStorage> {
    @Resource
    private TPurchaseStorageMapper purchaseStorageMapper;

    public PurchaseStorageRepository() {
        init("purStorage::");
    }

    public TPurchaseStorage find(int sid) {
        TPurchaseStorage purchaseStorage = getCache(sid, TPurchaseStorage.class);
        if (null != purchaseStorage) {
            return purchaseStorage;
        }

        // 缓存没有就查询数据库
        TPurchaseStorageExample example = new TPurchaseStorageExample();
        example.or().andSidEqualTo(sid);
        purchaseStorage = purchaseStorageMapper.selectOneByExample(example);
        if (null != purchaseStorage) {
            setCache(sid, purchaseStorage);
        }
        return purchaseStorage;
    }

    public List<TPurchaseStorage> findByPid(int pid) {
        TPurchaseStorageExample example = new TPurchaseStorageExample();
        example.or().andPidEqualTo(pid);
        return purchaseStorageMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TPurchaseStorageExample example = new TPurchaseStorageExample();
        example.or().andPidEqualTo(pid);
        return null != purchaseStorageMapper.selectOneByExample(example);
    }

    public boolean insert(int pid, int sid) {
        TPurchaseStorage row = new TPurchaseStorage();
        row.setPid(pid);
        row.setSid(sid);
        if (purchaseStorageMapper.insert(row) > 0) {
            setCache(row.getSid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TPurchaseStorage row) {
        if (purchaseStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getSid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TPurchaseStorage purchaseStorage = purchaseStorageMapper.selectByPrimaryKey(id);
        if (null == purchaseStorage) {
            return false;
        }
        delCache(purchaseStorage.getSid());
        return purchaseStorageMapper.deleteByPrimaryKey(id) > 0;
    }
}
