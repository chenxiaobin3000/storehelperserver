package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductAttachmentMapper;
import com.cxb.storehelperserver.model.TProductAttachment;
import com.cxb.storehelperserver.model.TProductAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 进货出入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class ProductAttachmentRepository extends BaseRepository<TProductAttachment> {
    @Resource
    private TProductAttachmentMapper productAttachmentMapper;

    public ProductAttachmentRepository() {
        init("productAtt::");
    }

    public TProductAttachment find(int id) {
        TProductAttachment productAttachment = getCache(id, TProductAttachment.class);
        if (null != productAttachment) {
            return productAttachment;
        }

        // 缓存没有就查询数据库
        productAttachment = productAttachmentMapper.selectByPrimaryKey(id);
        if (null != productAttachment) {
            setCache(id, productAttachment);
        }
        return productAttachment;
    }

    public List<TProductAttachment> findByOid(int oid) {
        TProductAttachmentExample example = new TProductAttachmentExample();
        example.or().andOidEqualTo(oid);
        return productAttachmentMapper.selectByExample(example);
    }

    public TProductAttachment insert(int oid, int imagesrc, String path, String name) {
        TProductAttachment row = new TProductAttachment();
        row.setOid(0);
        row.setSrc(imagesrc);
        row.setPath(path);
        row.setName(name);
        if (productAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return row;
        }
        return null;
    }

    public boolean update(TProductAttachment row) {
        if (productAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid) {
        val attrs = findByOid(oid);
        for (TProductAttachment a : attrs) {
            delCache(a.getId());
            if (productAttachmentMapper.deleteByPrimaryKey(a.getId()) <= 0) {
                return false;
            }
        }
        return true;
    }
}
