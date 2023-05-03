package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseStorageMapper;
import com.cxb.storehelperserver.model.TPurchaseStorage;
import com.cxb.storehelperserver.model.TPurchaseStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 采购仓库关联仓库
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

    public TPurchaseStorage find(int oid) {
        TPurchaseStorage purchaseStorage = getCache(oid, TPurchaseStorage.class);
        if (null != purchaseStorage) {
            return purchaseStorage;
        }

        // 缓存没有就查询数据库
        TPurchaseStorageExample example = new TPurchaseStorageExample();
        example.or().andOidEqualTo(oid);
        purchaseStorage = purchaseStorageMapper.selectOneByExample(example);
        if (null != purchaseStorage) {
            setCache(oid, purchaseStorage);
        }
        return purchaseStorage;
    }

    public TPurchaseStorage findBySid(int sid) {
        TPurchaseStorageExample example = new TPurchaseStorageExample();
        example.or().andSidEqualTo(sid);
        return purchaseStorageMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int sid) {
        TPurchaseStorage row = new TPurchaseStorage();
        row.setOid(oid);
        row.setSid(sid);
        if (purchaseStorageMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TPurchaseStorage row) {
        if (purchaseStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int sid) {
        delCache(oid);
        TPurchaseStorageExample example = new TPurchaseStorageExample();
        example.or().andOidEqualTo(oid).andSidEqualTo(sid);
        return purchaseStorageMapper.deleteByExample(example) > 0;
    }
}
