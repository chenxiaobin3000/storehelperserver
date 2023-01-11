package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSsInAttachmentMapper;
import com.cxb.storehelperserver.model.TSsInAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 标品入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class SsInAttachmentRepository extends BaseRepository<TSsInAttachment> {
    @Resource
    private TSsInAttachmentMapper ssInAttachmentMapper;

    public SsInAttachmentRepository() {
        init("ssInAtt::");
    }

    public TSsInAttachment find(int id) {
        TSsInAttachment ssInAttachment = getCache(id, TSsInAttachment.class);
        if (null != ssInAttachment) {
            return ssInAttachment;
        }

        // 缓存没有就查询数据库
        ssInAttachment = ssInAttachmentMapper.selectByPrimaryKey(id);
        if (null != ssInAttachment) {
            setCache(id, ssInAttachment);
        }
        return ssInAttachment;
    }

    public boolean insert(TSsInAttachment row) {
        if (ssInAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSsInAttachment row) {
        if (ssInAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return ssInAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
