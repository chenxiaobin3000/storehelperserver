package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSaleAttachmentMapper;
import com.cxb.storehelperserver.model.TSaleAttachment;
import com.cxb.storehelperserver.model.TSaleAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 销售出入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class SaleAttachmentRepository extends BaseRepository<List> {
    @Resource
    private TSaleAttachmentMapper saleAttachmentMapper;

    public SaleAttachmentRepository() {
        init("saleAtt::");
    }

    public TSaleAttachment find(int id) {
        return saleAttachmentMapper.selectByPrimaryKey(id);
    }

    public List<TSaleAttachment> findByOid(int oid) {
        List<TSaleAttachment> saleAttachments = getCache(oid, List.class);
        if (null != saleAttachments) {
            return saleAttachments;
        }

        // 缓存没有就查询数据库
        TSaleAttachmentExample example = new TSaleAttachmentExample();
        example.or().andOidEqualTo(oid);
        saleAttachments = saleAttachmentMapper.selectByExample(example);
        if (null != saleAttachments) {
            setCache(oid, saleAttachments);
        }
        return saleAttachments;
    }

    public TSaleAttachment insert(int oid, int imagesrc, String path, String name) {
        TSaleAttachment row = new TSaleAttachment();
        row.setOid(oid);
        row.setSrc(imagesrc);
        row.setPath(path);
        row.setName(name);
        if (saleAttachmentMapper.insert(row) > 0) {
            delCache(oid);
            return row;
        }
        return null;
    }

    public boolean update(TSaleAttachment row) {
        if (saleAttachmentMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int id) {
        delCache(oid);
        return saleAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean deleteByOid(int oid) {
        delCache(oid);
        TSaleAttachmentExample example = new TSaleAttachmentExample();
        example.or().andOidEqualTo(oid);
        return saleAttachmentMapper.deleteByExample(example) > 0;
    }
}
