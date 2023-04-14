package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageBackMapper;
import com.cxb.storehelperserver.model.TStorageBack;
import com.cxb.storehelperserver.model.TStorageBackExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 线下退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StorageBackRepository extends BaseRepository<TStorageBack> {
    @Resource
    private TStorageBackMapper storageBackMapper;

    public StorageBackRepository() {
        init("storageBack::");
    }

    public TStorageBack find(int oid) {
        TStorageBack storageBack = getCache(oid, TStorageBack.class);
        if (null != storageBack) {
            return storageBack;
        }

        // 缓存没有就查询数据库
        TStorageBackExample example = new TStorageBackExample();
        example.or().andOidEqualTo(oid);
        storageBack = storageBackMapper.selectOneByExample(example);
        if (null != storageBack) {
            setCache(oid, storageBack);
        }
        return storageBack;
    }

    public List<TStorageBack> findByPid(int pid) {
        TStorageBackExample example = new TStorageBackExample();
        example.or().andPidEqualTo(pid);
        return storageBackMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TStorageBackExample example = new TStorageBackExample();
        example.or().andPidEqualTo(pid);
        return null != storageBackMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int pid) {
        TStorageBack row = new TStorageBack();
        row.setOid(oid);
        row.setPid(pid);
        if (storageBackMapper.insert(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageBack row) {
        if (storageBackMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageBack storageBack = storageBackMapper.selectByPrimaryKey(id);
        if (null == storageBack) {
            return false;
        }
        delCache(storageBack.getOid());
        return storageBackMapper.deleteByPrimaryKey(id) > 0;
    }
}
