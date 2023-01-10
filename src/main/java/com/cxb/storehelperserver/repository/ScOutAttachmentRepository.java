package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TScOutAttachmentMapper;
import com.cxb.storehelperserver.model.TScOutAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 商品出库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class ScOutAttachmentRepository extends BaseRepository<TScOutAttachment> {
    @Resource
    private TScOutAttachmentMapper scOutAttachmentMapper;

    public ScOutAttachmentRepository() {
        init("scOutAtt::");
    }

    public TScOutAttachment find(int id) {
        TScOutAttachment scOutAttachment = getCache(id, TScOutAttachment.class);
        if (null != scOutAttachment) {
            return scOutAttachment;
        }

        // 缓存没有就查询数据库
        scOutAttachment = scOutAttachmentMapper.selectByPrimaryKey(id);
        if (null != scOutAttachment) {
            setCache(id, scOutAttachment);
        }
        return scOutAttachment;
    }

    public boolean insert(TScOutAttachment row) {
        if (scOutAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TScOutAttachment row) {
        if (scOutAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return scOutAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
