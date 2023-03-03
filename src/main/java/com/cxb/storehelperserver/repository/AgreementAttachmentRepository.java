package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementAttachmentMapper;
import com.cxb.storehelperserver.model.TAgreementAttachment;
import com.cxb.storehelperserver.model.TAgreementAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 履约出入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class AgreementAttachmentRepository extends BaseRepository<List> {
    @Resource
    private TAgreementAttachmentMapper agreementAttachmentMapper;

    public AgreementAttachmentRepository() {
        init("agreeAtt::");
    }

    public TAgreementAttachment find(int id) {
        return agreementAttachmentMapper.selectByPrimaryKey(id);
    }

    public List<TAgreementAttachment> findByOid(int oid) {
        List<TAgreementAttachment> agreementAttachments = getCache(oid, List.class);
        if (null != agreementAttachments) {
            return agreementAttachments;
        }

        // 缓存没有就查询数据库
        TAgreementAttachmentExample example = new TAgreementAttachmentExample();
        example.or().andOidEqualTo(oid);
        agreementAttachments = agreementAttachmentMapper.selectByExample(example);
        if (null != agreementAttachments) {
            setCache(oid, agreementAttachments);
        }
        return agreementAttachments;
    }

    public TAgreementAttachment insert(int oid, int imagesrc, String path, String name) {
        TAgreementAttachment row = new TAgreementAttachment();
        row.setOid(oid);
        row.setSrc(imagesrc);
        row.setPath(path);
        row.setName(name);
        if (agreementAttachmentMapper.insert(row) > 0) {
            delCache(oid);
            return row;
        }
        return null;
    }

    public boolean update(TAgreementAttachment row) {
        if (agreementAttachmentMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int id) {
        delCache(oid);
        return agreementAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean deleteByOid(int oid) {
        delCache(oid);
        TAgreementAttachmentExample example = new TAgreementAttachmentExample();
        example.or().andOidEqualTo(oid);
        return agreementAttachmentMapper.deleteByExample(example) > 0;
    }
}
