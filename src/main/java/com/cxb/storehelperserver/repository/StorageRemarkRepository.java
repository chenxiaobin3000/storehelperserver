package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageRemarkMapper;
import com.cxb.storehelperserver.model.TStorageRemark;
import com.cxb.storehelperserver.model.TStorageRemarkExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 生产备注关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StorageRemarkRepository extends BaseRepository<List> {
    @Resource
    private TStorageRemarkMapper storageRemarkMapper;

    public StorageRemarkRepository() {
        init("storageRemark::");
    }

    public TStorageRemark find(int id) {
        return storageRemarkMapper.selectByPrimaryKey(id);
    }

    public List<TStorageRemark> findByOid(int oid) {
        List<TStorageRemark> storageRemarks = getCache(oid, List.class);
        if (null != storageRemarks) {
            return storageRemarks;
        }

        // 缓存没有就查询数据库
        TStorageRemarkExample example = new TStorageRemarkExample();
        example.or().andOidEqualTo(oid);
        storageRemarks = storageRemarkMapper.selectByExample(example);
        if (null != storageRemarks) {
            setCache(oid, storageRemarks);
        }
        return storageRemarks;
    }

    public boolean insert(int oid, String remark, Date cdate) {
        TStorageRemark row = new TStorageRemark();
        row.setOid(oid);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (storageRemarkMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TStorageRemark row) {
        if (storageRemarkMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageRemark storageRemark = storageRemarkMapper.selectByPrimaryKey(id);
        if (null == storageRemark) {
            return false;
        }
        delCache(storageRemark.getOid());
        return storageRemarkMapper.deleteByPrimaryKey(id) > 0;
    }
}
