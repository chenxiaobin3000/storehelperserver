package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementAttachmentMapper;
import com.cxb.storehelperserver.model.TAgreementAttachment;
import com.cxb.storehelperserver.model.TAgreementAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class AgreementAttachmentRepository extends BaseRepository<TAgreementAttachment> {
    @Resource
    private TAgreementAttachmentMapper agreementAttachmentMapper;

    public AgreementAttachmentRepository() {
        init("agreeAtt::");
    }

    public TAgreementAttachment find(int id) {
        TAgreementAttachment agreementAttachment = getCache(id, TAgreementAttachment.class);
        if (null != agreementAttachment) {
            return agreementAttachment;
        }

        // 缓存没有就查询数据库
        agreementAttachment = agreementAttachmentMapper.selectByPrimaryKey(id);
        if (null != agreementAttachment) {
            setCache(id, agreementAttachment);
        }
        return agreementAttachment;
    }

    public List<TAgreementAttachment> findByOid(int oid) {
        TAgreementAttachmentExample example = new TAgreementAttachmentExample();
        example.or().andOidEqualTo(oid);
        return agreementAttachmentMapper.selectByExample(example);
    }

    public boolean insert(TAgreementAttachment row) {
        if (agreementAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementAttachment row) {
        if (agreementAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid) {
        val attrs = findByOid(oid);
        for (TAgreementAttachment a : attrs) {
            delCache(a.getId());
            if (agreementAttachmentMapper.deleteByPrimaryKey(a.getId()) <= 0) {
                return false;
            }
        }
        return true;
    }
}
