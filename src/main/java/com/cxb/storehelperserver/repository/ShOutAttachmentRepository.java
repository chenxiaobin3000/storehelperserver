package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TShOutAttachmentMapper;
import com.cxb.storehelperserver.model.TShOutAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 半成品出库附件仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class ShOutAttachmentRepository extends BaseRepository<TShOutAttachment> {
    @Resource
    private TShOutAttachmentMapper shOutAttachmentMapper;

    public ShOutAttachmentRepository() {
        init("shOutAtt::");
    }

    public TShOutAttachment find(int id) {
        TShOutAttachment shOutAttachment = getCache(id, TShOutAttachment.class);
        if (null != shOutAttachment) {
            return shOutAttachment;
        }

        // 缓存没有就查询数据库
        shOutAttachment = shOutAttachmentMapper.selectByPrimaryKey(id);
        if (null != shOutAttachment) {
            setCache(id, shOutAttachment);
        }
        return shOutAttachment;
    }

    public boolean insert(TShOutAttachment row) {
        if (shOutAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TShOutAttachment row) {
        if (shOutAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return shOutAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
