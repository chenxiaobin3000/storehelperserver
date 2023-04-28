package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOfflineAttachmentMapper;
import com.cxb.storehelperserver.model.TOfflineAttachment;
import com.cxb.storehelperserver.model.TOfflineAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 线下销售出入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class OfflineAttachmentRepository extends BaseRepository<List> {
    @Resource
    private TOfflineAttachmentMapper offlineAttachmentMapper;

    public OfflineAttachmentRepository() {
        init("offlineAtt::");
    }

    public TOfflineAttachment find(int id) {
        return offlineAttachmentMapper.selectByPrimaryKey(id);
    }

    public List<TOfflineAttachment> findByOid(int oid) {
        List<TOfflineAttachment> offlineAttachments = getCache(oid, List.class);
        if (null != offlineAttachments) {
            return offlineAttachments;
        }

        // 缓存没有就查询数据库
        TOfflineAttachmentExample example = new TOfflineAttachmentExample();
        example.or().andOidEqualTo(oid);
        offlineAttachments = offlineAttachmentMapper.selectByExample(example);
        if (null != offlineAttachments) {
            setCache(oid, offlineAttachments);
        }
        return offlineAttachments;
    }

    public TOfflineAttachment insert(int oid, int imagesrc, String path, String name) {
        TOfflineAttachment row = new TOfflineAttachment();
        row.setOid(oid);
        row.setSrc(imagesrc);
        row.setPath(path);
        row.setName(name);
        if (offlineAttachmentMapper.insert(row) > 0) {
            delCache(oid);
            return row;
        }
        return null;
    }

    public boolean update(TOfflineAttachment row) {
        if (offlineAttachmentMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int id) {
        delCache(oid);
        return offlineAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean deleteByOid(int oid) {
        delCache(oid);
        TOfflineAttachmentExample example = new TOfflineAttachmentExample();
        example.or().andOidEqualTo(oid);
        return offlineAttachmentMapper.deleteByExample(example) > 0;
    }
}
