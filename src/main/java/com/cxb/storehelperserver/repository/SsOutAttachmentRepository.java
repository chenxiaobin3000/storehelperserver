package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSsOutAttachmentMapper;
import com.cxb.storehelperserver.model.TSsOutAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 标品出库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class SsOutAttachmentRepository extends BaseRepository<TSsOutAttachment> {
    @Resource
    private TSsOutAttachmentMapper sdOutAttachmentMapper;

    public SsOutAttachmentRepository() {
        init("sdOutAtt::");
    }

    public TSsOutAttachment find(int id) {
        TSsOutAttachment sdOutAttachment = getCache(id, TSsOutAttachment.class);
        if (null != sdOutAttachment) {
            return sdOutAttachment;
        }

        // 缓存没有就查询数据库
        sdOutAttachment = sdOutAttachmentMapper.selectByPrimaryKey(id);
        if (null != sdOutAttachment) {
            setCache(id, sdOutAttachment);
        }
        return sdOutAttachment;
    }

    public boolean insert(TSsOutAttachment row) {
        if (sdOutAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSsOutAttachment row) {
        if (sdOutAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return sdOutAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
