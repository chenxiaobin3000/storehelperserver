package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageAttachmentMapper;
import com.cxb.storehelperserver.model.TStorageAttachment;
import com.cxb.storehelperserver.model.TStorageAttachmentExample;
import lombok.extern.slf4j.Slf4j;
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
public class StorageAttachmentRepository extends BaseRepository<List> {
    @Resource
    private TStorageAttachmentMapper storageAttachmentMapper;

    public StorageAttachmentRepository() {
        init("storageAtt::");
    }

    public TStorageAttachment find(int id) {
        return storageAttachmentMapper.selectByPrimaryKey(id);
    }

    public List<TStorageAttachment> findByOid(int oid) {
        List<TStorageAttachment> storageAttachments = getCache(oid, List.class);
        if (null != storageAttachments) {
            return storageAttachments;
        }

        // 缓存没有就查询数据库
        TStorageAttachmentExample example = new TStorageAttachmentExample();
        example.or().andOidEqualTo(oid);
        storageAttachments = storageAttachmentMapper.selectByExample(example);
        if (null != storageAttachments) {
            setCache(oid, storageAttachments);
        }
        return storageAttachments;
    }

    public TStorageAttachment insert(int oid, int imagesrc, String path, String name) {
        TStorageAttachment row = new TStorageAttachment();
        row.setOid(oid);
        row.setSrc(imagesrc);
        row.setPath(path);
        row.setName(name);
        if (storageAttachmentMapper.insert(row) > 0) {
            delCache(oid);
            return row;
        }
        return null;
    }

    public boolean update(TStorageAttachment row) {
        if (storageAttachmentMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int id) {
        delCache(oid);
        return storageAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean deleteByOid(int oid) {
        delCache(oid);
        TStorageAttachmentExample example = new TStorageAttachmentExample();
        example.or().andOidEqualTo(oid);
        return storageAttachmentMapper.deleteByExample(example) > 0;
    }
}
