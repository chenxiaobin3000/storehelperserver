package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSdOutAttachmentMapper;
import com.cxb.storehelperserver.model.TSdOutAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 废料出库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class SdOutAttachmentRepository extends BaseRepository<TSdOutAttachment> {
    @Resource
    private TSdOutAttachmentMapper sdOutAttachmentMapper;

    public SdOutAttachmentRepository() {
        init("sdOutAtt::");
    }

    public TSdOutAttachment find(int id) {
        TSdOutAttachment sdOutAttachment = getCache(id, TSdOutAttachment.class);
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

    public boolean insert(TSdOutAttachment row) {
        if (sdOutAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSdOutAttachment row) {
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
