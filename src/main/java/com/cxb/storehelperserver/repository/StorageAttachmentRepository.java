package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageAttachmentMapper;
import com.cxb.storehelperserver.model.TStorageAttachment;
import com.cxb.storehelperserver.model.TStorageAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 进货出入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class StorageAttachmentRepository extends BaseRepository<TStorageAttachment> {
    @Resource
    private TStorageAttachmentMapper storageAttachmentMapper;

    public StorageAttachmentRepository() {
        init("storageAtt::");
    }

    public TStorageAttachment find(int id) {
        TStorageAttachment storageAttachment = getCache(id, TStorageAttachment.class);
        if (null != storageAttachment) {
            return storageAttachment;
        }

        // 缓存没有就查询数据库
        storageAttachment = storageAttachmentMapper.selectByPrimaryKey(id);
        if (null != storageAttachment) {
            setCache(id, storageAttachment);
        }
        return storageAttachment;
    }

    public List<TStorageAttachment> findByOid(int oid) {
        TStorageAttachmentExample example = new TStorageAttachmentExample();
        example.or().andOidEqualTo(oid);
        return storageAttachmentMapper.selectByExample(example);
    }

    public boolean insert(TStorageAttachment row) {
        if (storageAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageAttachment row) {
        if (storageAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid) {
        val attrs = findByOid(oid);
        for (TStorageAttachment a : attrs) {
            delCache(a.getId());
            if (storageAttachmentMapper.deleteByPrimaryKey(a.getId()) <= 0) {
                return false;
            }
        }
        return true;
    }
}
