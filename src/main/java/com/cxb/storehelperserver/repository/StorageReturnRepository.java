package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageReturnMapper;
import com.cxb.storehelperserver.model.TStorageReturn;
import com.cxb.storehelperserver.model.TStorageReturnExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StorageReturnRepository extends BaseRepository<TStorageReturn> {
    @Resource
    private TStorageReturnMapper storageReturnMapper;

    public StorageReturnRepository() {
        init("storageRet::");
    }

    public TStorageReturn find(int oid) {
        TStorageReturn storageReturn = getCache(oid, TStorageReturn.class);
        if (null != storageReturn) {
            return storageReturn;
        }

        // 缓存没有就查询数据库
        TStorageReturnExample example = new TStorageReturnExample();
        example.or().andOidEqualTo(oid);
        storageReturn = storageReturnMapper.selectOneByExample(example);
        if (null != storageReturn) {
            setCache(oid, storageReturn);
        }
        return storageReturn;
    }

    public List<TStorageReturn> findByPid(int pid) {
        TStorageReturnExample example = new TStorageReturnExample();
        example.or().andPidEqualTo(pid);
        return storageReturnMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TStorageReturnExample example = new TStorageReturnExample();
        example.or().andPidEqualTo(pid);
        return null != storageReturnMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int pid) {
        TStorageReturn row = new TStorageReturn();
        row.setOid(oid);
        row.setPid(pid);
        if (storageReturnMapper.insert(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageReturn row) {
        if (storageReturnMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageReturn storageReturn = storageReturnMapper.selectByPrimaryKey(id);
        if (null == storageReturn) {
            return false;
        }
        delCache(storageReturn.getOid());
        return storageReturnMapper.deleteByPrimaryKey(id) > 0;
    }
}
