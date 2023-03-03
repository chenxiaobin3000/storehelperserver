package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseAttachmentMapper;
import com.cxb.storehelperserver.model.TPurchaseAttachment;
import com.cxb.storehelperserver.model.TPurchaseAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 采购出入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class PurchaseAttachmentRepository extends BaseRepository<List> {
    @Resource
    private TPurchaseAttachmentMapper purchaseAttachmentMapper;

    public PurchaseAttachmentRepository() {
        init("purAtt::");
    }

    public TPurchaseAttachment find(int id) {
        return purchaseAttachmentMapper.selectByPrimaryKey(id);
    }

    public List<TPurchaseAttachment> findByOid(int oid) {
        List<TPurchaseAttachment> purchaseAttachments = getCache(oid, List.class);
        if (null != purchaseAttachments) {
            return purchaseAttachments;
        }

        // 缓存没有就查询数据库
        TPurchaseAttachmentExample example = new TPurchaseAttachmentExample();
        example.or().andOidEqualTo(oid);
        purchaseAttachments = purchaseAttachmentMapper.selectByExample(example);
        if (null != purchaseAttachments) {
            setCache(oid, purchaseAttachments);
        }
        return purchaseAttachments;
    }

    public TPurchaseAttachment insert(int oid, int imagesrc, String path, String name) {
        TPurchaseAttachment row = new TPurchaseAttachment();
        row.setOid(oid);
        row.setSrc(imagesrc);
        row.setPath(path);
        row.setName(name);
        if (purchaseAttachmentMapper.insert(row) > 0) {
            delCache(oid);
            return row;
        }
        return null;
    }

    public boolean update(TPurchaseAttachment row) {
        if (purchaseAttachmentMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int id) {
        delCache(oid);
        return purchaseAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean deleteByOid(int oid) {
        delCache(oid);
        TPurchaseAttachmentExample example = new TPurchaseAttachmentExample();
        example.or().andOidEqualTo(oid);
        return purchaseAttachmentMapper.deleteByExample(example) > 0;
    }
}
