package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductStorageMapper;
import com.cxb.storehelperserver.model.TProductStorage;
import com.cxb.storehelperserver.model.TProductStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 生产仓储关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class ProductStorageRepository extends BaseRepository<TProductStorage> {
    @Resource
    private TProductStorageMapper productStorageMapper;

    public ProductStorageRepository() {
        init("productStor::");
    }

    public TProductStorage find(int oid) {
        TProductStorage productStorage = getCache(oid, TProductStorage.class);
        if (null != productStorage) {
            return productStorage;
        }

        // 缓存没有就查询数据库
        TProductStorageExample example = new TProductStorageExample();
        example.or().andOidEqualTo(oid);
        productStorage = productStorageMapper.selectOneByExample(example);
        if (null != productStorage) {
            setCache(oid, productStorage);
        }
        return productStorage;
    }

    public boolean insert(int oid, int sid) {
        TProductStorage row = new TProductStorage();
        row.setOid(oid);
        row.setSid(sid);
        if (productStorageMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TProductStorage row) {
        if (productStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int sid) {
        delCache(oid);
        TProductStorageExample example = new TProductStorageExample();
        example.or().andOidEqualTo(oid).andSidEqualTo(sid);
        return productStorageMapper.deleteByExample(example) > 0;
    }
}
