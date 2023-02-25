package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageDispatchMapper;
import com.cxb.storehelperserver.model.TStorageDispatch;
import com.cxb.storehelperserver.model.TStorageDispatchExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 入库与调度关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StorageDispatchRepository extends BaseRepository<TStorageDispatch> {
    @Resource
    private TStorageDispatchMapper storageDispatchMapper;

    public StorageDispatchRepository() {
        init("storDispatch::");
    }

    public TStorageDispatch find(int oid) {
        TStorageDispatch storageDispatch = getCache(oid, TStorageDispatch.class);
        if (null != storageDispatch) {
            return storageDispatch;
        }

        // 缓存没有就查询数据库
        TStorageDispatchExample example = new TStorageDispatchExample();
        example.or().andOidEqualTo(oid);
        storageDispatch = storageDispatchMapper.selectOneByExample(example);
        if (null != storageDispatch) {
            setCache(oid, storageDispatch);
        }
        return storageDispatch;
    }

    public List<TStorageDispatch> findByDid(int did) {
        TStorageDispatchExample example = new TStorageDispatchExample();
        example.or().andDidEqualTo(did);
        return storageDispatchMapper.selectByExample(example);
    }

    public boolean checkByDid(int did) {
        TStorageDispatchExample example = new TStorageDispatchExample();
        example.or().andDidEqualTo(did);
        return null != storageDispatchMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int did) {
        TStorageDispatch row = new TStorageDispatch();
        row.setOid(oid);
        row.setDid(did);
        if (storageDispatchMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageDispatch row) {
        if (storageDispatchMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int did) {
        delCache(oid);
        TStorageDispatchExample example = new TStorageDispatchExample();
        example.or().andOidEqualTo(oid).andDidEqualTo(did);
        return storageDispatchMapper.deleteByExample(example) > 0;
    }
}
