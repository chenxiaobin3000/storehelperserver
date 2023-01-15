package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TShInAttachmentMapper;
import com.cxb.storehelperserver.model.TShInAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 半成品入库附件仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class ShInAttachmentRepository extends BaseRepository<TShInAttachment> {
    @Resource
    private TShInAttachmentMapper shInAttachmentMapper;

    public ShInAttachmentRepository() {
        init("shInAtt::");
    }

    public TShInAttachment find(int id) {
        TShInAttachment shInAttachment = getCache(id, TShInAttachment.class);
        if (null != shInAttachment) {
            return shInAttachment;
        }

        // 缓存没有就查询数据库
        shInAttachment = shInAttachmentMapper.selectByPrimaryKey(id);
        if (null != shInAttachment) {
            setCache(id, shInAttachment);
        }
        return shInAttachment;
    }

    public boolean insert(TShInAttachment row) {
        if (shInAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TShInAttachment row) {
        if (shInAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return shInAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
