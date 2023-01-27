package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductOrderAttachmentMapper;
import com.cxb.storehelperserver.model.TProductOrderAttachment;
import com.cxb.storehelperserver.model.TProductOrderAttachmentExample;
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
public class ProductOrderAttachmentRepository extends BaseRepository<TProductOrderAttachment> {
    @Resource
    private TProductOrderAttachmentMapper productOrderAttachmentMapper;

    public ProductOrderAttachmentRepository() {
        init("poAtt::");
    }

    public TProductOrderAttachment find(int id) {
        TProductOrderAttachment productOrderAttachment = getCache(id, TProductOrderAttachment.class);
        if (null != productOrderAttachment) {
            return productOrderAttachment;
        }

        // 缓存没有就查询数据库
        productOrderAttachment = productOrderAttachmentMapper.selectByPrimaryKey(id);
        if (null != productOrderAttachment) {
            setCache(id, productOrderAttachment);
        }
        return productOrderAttachment;
    }

    public List<TProductOrderAttachment> findByOid(int oid) {
        TProductOrderAttachmentExample example = new TProductOrderAttachmentExample();
        example.or().andOidEqualTo(oid);
        return productOrderAttachmentMapper.selectByExample(example);
    }

    public boolean insert(TProductOrderAttachment row) {
        if (productOrderAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TProductOrderAttachment row) {
        if (productOrderAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid) {
        val attrs = findByOid(oid);
        for (TProductOrderAttachment a : attrs) {
            delCache(a.getId());
            if (productOrderAttachmentMapper.deleteByPrimaryKey(a.getId()) <= 0) {
                return false;
            }
        }
        return true;
    }
}
