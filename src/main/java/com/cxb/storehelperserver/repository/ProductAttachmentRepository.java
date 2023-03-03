package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductAttachmentMapper;
import com.cxb.storehelperserver.model.TProductAttachment;
import com.cxb.storehelperserver.model.TProductAttachmentExample;
import lombok.extern.slf4j.Slf4j;
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
public class ProductAttachmentRepository extends BaseRepository<List> {
    @Resource
    private TProductAttachmentMapper productAttachmentMapper;

    public ProductAttachmentRepository() {
        init("productAtt::");
    }

    public TProductAttachment find(int id) {
        return productAttachmentMapper.selectByPrimaryKey(id);
    }

    public List<TProductAttachment> findByOid(int oid) {
        List<TProductAttachment> productAttachments = getCache(oid, List.class);
        if (null != productAttachments) {
            return productAttachments;
        }

        // 缓存没有就查询数据库
        TProductAttachmentExample example = new TProductAttachmentExample();
        example.or().andOidEqualTo(oid);
        productAttachments = productAttachmentMapper.selectByExample(example);
        if (null != productAttachments) {
            setCache(oid, productAttachments);
        }
        return productAttachments;
    }

    public TProductAttachment insert(int oid, int imagesrc, String path, String name) {
        TProductAttachment row = new TProductAttachment();
        row.setOid(oid);
        row.setSrc(imagesrc);
        row.setPath(path);
        row.setName(name);
        if (productAttachmentMapper.insert(row) > 0) {
            delCache(oid);
            return row;
        }
        return null;
    }

    public boolean update(TProductAttachment row) {
        if (productAttachmentMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int id) {
        delCache(oid);
        return productAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean deleteByOid(int oid) {
        delCache(oid);
        TProductAttachmentExample example = new TProductAttachmentExample();
        example.or().andOidEqualTo(oid);
        return productAttachmentMapper.deleteByExample(example) > 0;
    }
}
