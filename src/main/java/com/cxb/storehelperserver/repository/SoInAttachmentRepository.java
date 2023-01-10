package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSoInAttachmentMapper;
import com.cxb.storehelperserver.model.TSoInAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class SoInAttachmentRepository extends BaseRepository<TSoInAttachment> {
    @Resource
    private TSoInAttachmentMapper soInAttachmentMapper;

    public SoInAttachmentRepository() {
        init("soInAtt::");
    }

    public TSoInAttachment find(int id) {
        TSoInAttachment soInAttachment = getCache(id, TSoInAttachment.class);
        if (null != soInAttachment) {
            return soInAttachment;
        }

        // 缓存没有就查询数据库
        soInAttachment = soInAttachmentMapper.selectByPrimaryKey(id);
        if (null != soInAttachment) {
            setCache(id, soInAttachment);
        }
        return soInAttachment;
    }

    public boolean insert(TSoInAttachment row) {
        if (soInAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSoInAttachment row) {
        if (soInAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return soInAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
