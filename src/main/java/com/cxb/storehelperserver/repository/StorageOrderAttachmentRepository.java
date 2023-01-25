package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageOrderAttachmentMapper;
import com.cxb.storehelperserver.model.TStorageOrderAttachment;
import com.cxb.storehelperserver.model.TStorageOrderAttachmentExample;
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
public class StorageOrderAttachmentRepository extends BaseRepository<TStorageOrderAttachment> {
    @Resource
    private TStorageOrderAttachmentMapper storageOrderAttachmentMapper;

    public StorageOrderAttachmentRepository() {
        init("soAtt::");
    }

    public TStorageOrderAttachment find(int id) {
        TStorageOrderAttachment storageOrderAttachment = getCache(id, TStorageOrderAttachment.class);
        if (null != storageOrderAttachment) {
            return storageOrderAttachment;
        }

        // 缓存没有就查询数据库
        storageOrderAttachment = storageOrderAttachmentMapper.selectByPrimaryKey(id);
        if (null != storageOrderAttachment) {
            setCache(id, storageOrderAttachment);
        }
        return storageOrderAttachment;
    }

    public List<TStorageOrderAttachment> findByOid(int oid) {
        TStorageOrderAttachmentExample example = new TStorageOrderAttachmentExample();
        example.or().andOidEqualTo(oid);
        return storageOrderAttachmentMapper.selectByExample(example);
    }

    public boolean insert(TStorageOrderAttachment row) {
        if (storageOrderAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageOrderAttachment row) {
        if (storageOrderAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return storageOrderAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
