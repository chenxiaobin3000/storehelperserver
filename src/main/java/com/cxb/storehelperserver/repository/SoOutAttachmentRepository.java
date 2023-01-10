package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSoOutAttachmentMapper;
import com.cxb.storehelperserver.model.TSoOutAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料出库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class SoOutAttachmentRepository extends BaseRepository<TSoOutAttachment> {
    @Resource
    private TSoOutAttachmentMapper soOutAttachmentMapper;

    public SoOutAttachmentRepository() {
        init("soOutAtt::");
    }

    public TSoOutAttachment find(int id) {
        TSoOutAttachment soOutAttachment = getCache(id, TSoOutAttachment.class);
        if (null != soOutAttachment) {
            return soOutAttachment;
        }

        // 缓存没有就查询数据库
        soOutAttachment = soOutAttachmentMapper.selectByPrimaryKey(id);
        if (null != soOutAttachment) {
            setCache(id, soOutAttachment);
        }
        return soOutAttachment;
    }

    public boolean insert(TSoOutAttachment row) {
        if (soOutAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSoOutAttachment row) {
        if (soOutAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return soOutAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
