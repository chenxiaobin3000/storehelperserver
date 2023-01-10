package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TScInAttachmentMapper;
import com.cxb.storehelperserver.model.TScInAttachment;
import com.cxb.storehelperserver.model.TScInAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 商品入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class ScInAttachmentRepository extends BaseRepository<TScInAttachment> {
    @Resource
    private TScInAttachmentMapper scInAttachmentMapper;

    public ScInAttachmentRepository() {
        init("scInAtt::");
    }

    public TScInAttachment find(int id) {
        TScInAttachment scInAttachment = getCache(id, TScInAttachment.class);
        if (null != scInAttachment) {
            return scInAttachment;
        }

        // 缓存没有就查询数据库
        scInAttachment = scInAttachmentMapper.selectByPrimaryKey(id);
        if (null != scInAttachment) {
            setCache(id, scInAttachment);
        }
        return scInAttachment;
    }

    public boolean insert(TScInAttachment row) {
        if (scInAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TScInAttachment row) {
        if (scInAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return scInAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
