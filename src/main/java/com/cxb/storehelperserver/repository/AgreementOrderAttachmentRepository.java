package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementOrderAttachmentMapper;
import com.cxb.storehelperserver.model.TAgreementOrderAttachment;
import com.cxb.storehelperserver.model.TAgreementOrderAttachmentExample;
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
public class AgreementOrderAttachmentRepository extends BaseRepository<TAgreementOrderAttachment> {
    @Resource
    private TAgreementOrderAttachmentMapper agreementOrderAttachmentMapper;

    public AgreementOrderAttachmentRepository() {
        init("aoAtt::");
    }

    public TAgreementOrderAttachment find(int id) {
        TAgreementOrderAttachment agreementOrderAttachment = getCache(id, TAgreementOrderAttachment.class);
        if (null != agreementOrderAttachment) {
            return agreementOrderAttachment;
        }

        // 缓存没有就查询数据库
        agreementOrderAttachment = agreementOrderAttachmentMapper.selectByPrimaryKey(id);
        if (null != agreementOrderAttachment) {
            setCache(id, agreementOrderAttachment);
        }
        return agreementOrderAttachment;
    }

    public List<TAgreementOrderAttachment> findByOid(int oid) {
        TAgreementOrderAttachmentExample example = new TAgreementOrderAttachmentExample();
        example.or().andOidEqualTo(oid);
        return agreementOrderAttachmentMapper.selectByExample(example);
    }

    public boolean insert(TAgreementOrderAttachment row) {
        if (agreementOrderAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementOrderAttachment row) {
        if (agreementOrderAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid) {
        val attrs = findByOid(oid);
        for (TAgreementOrderAttachment a : attrs) {
            delCache(a.getId());
            if (agreementOrderAttachmentMapper.deleteByPrimaryKey(a.getId()) <= 0) {
                return false;
            }
        }
        return true;
    }
}
