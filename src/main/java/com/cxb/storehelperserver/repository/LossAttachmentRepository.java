package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TLossAttachmentMapper;
import com.cxb.storehelperserver.model.TLossAttachment;
import com.cxb.storehelperserver.model.TLossAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 损耗出入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class LossAttachmentRepository extends BaseRepository<TLossAttachment> {
    @Resource
    private TLossAttachmentMapper lossAttachmentMapper;

    public LossAttachmentRepository() {
        init("lossAtt::");
    }

    public TLossAttachment find(int id) {
        TLossAttachment lossAttachment = getCache(id, TLossAttachment.class);
        if (null != lossAttachment) {
            return lossAttachment;
        }

        // 缓存没有就查询数据库
        lossAttachment = lossAttachmentMapper.selectByPrimaryKey(id);
        if (null != lossAttachment) {
            setCache(id, lossAttachment);
        }
        return lossAttachment;
    }

    public List<TLossAttachment> findByOid(int oid) {
        TLossAttachmentExample example = new TLossAttachmentExample();
        example.or().andOidEqualTo(oid);
        return lossAttachmentMapper.selectByExample(example);
    }

    public boolean insert(TLossAttachment row) {
        if (lossAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TLossAttachment row) {
        if (lossAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid) {
        val attrs = findByOid(oid);
        for (TLossAttachment a : attrs) {
            delCache(a.getId());
            if (lossAttachmentMapper.deleteByPrimaryKey(a.getId()) <= 0) {
                return false;
            }
        }
        return true;
    }
}
