package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseAttachmentMapper;
import com.cxb.storehelperserver.model.TPurchaseAttachment;
import com.cxb.storehelperserver.model.TPurchaseAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class PurchaseAttachmentRepository extends BaseRepository<TPurchaseAttachment> {
    @Resource
    private TPurchaseAttachmentMapper purchaseAttachmentMapper;

    public PurchaseAttachmentRepository() {
        init("purAtt::");
    }

    public TPurchaseAttachment find(int id) {
        TPurchaseAttachment purchaseAttachment = getCache(id, TPurchaseAttachment.class);
        if (null != purchaseAttachment) {
            return purchaseAttachment;
        }

        // 缓存没有就查询数据库
        purchaseAttachment = purchaseAttachmentMapper.selectByPrimaryKey(id);
        if (null != purchaseAttachment) {
            setCache(id, purchaseAttachment);
        }
        return purchaseAttachment;
    }

    public List<TPurchaseAttachment> findByOid(int oid) {
        TPurchaseAttachmentExample example = new TPurchaseAttachmentExample();
        example.or().andOidEqualTo(oid);
        return purchaseAttachmentMapper.selectByExample(example);
    }

    public TPurchaseAttachment insert(int oid, int imagesrc, String path, String name) {
        TPurchaseAttachment row = new TPurchaseAttachment();
        row.setOid(oid);
        row.setSrc(imagesrc);
        row.setPath(path);
        row.setName(name);
        if (purchaseAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return row;
        }
        return null;
    }

    public boolean update(TPurchaseAttachment row) {
        if (purchaseAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid) {
        val attrs = findByOid(oid);
        for (TPurchaseAttachment a : attrs) {
            delCache(a.getId());
            if (purchaseAttachmentMapper.deleteByPrimaryKey(a.getId()) <= 0) {
                return false;
            }
        }
        return true;
    }
}
