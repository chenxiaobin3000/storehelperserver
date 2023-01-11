package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSdInAttachmentMapper;
import com.cxb.storehelperserver.model.TSdInAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 废料入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class SdInAttachmentRepository extends BaseRepository<TSdInAttachment> {
    @Resource
    private TSdInAttachmentMapper sdInAttachmentMapper;

    public SdInAttachmentRepository() {
        init("sdInAtt::");
    }

    public TSdInAttachment find(int id) {
        TSdInAttachment sdInAttachment = getCache(id, TSdInAttachment.class);
        if (null != sdInAttachment) {
            return sdInAttachment;
        }

        // 缓存没有就查询数据库
        sdInAttachment = sdInAttachmentMapper.selectByPrimaryKey(id);
        if (null != sdInAttachment) {
            setCache(id, sdInAttachment);
        }
        return sdInAttachment;
    }

    public boolean insert(TSdInAttachment row) {
        if (sdInAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSdInAttachment row) {
        if (sdInAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return sdInAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
