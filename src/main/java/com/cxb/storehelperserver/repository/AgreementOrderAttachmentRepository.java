package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementOrderAttachmentMapper;
import com.cxb.storehelperserver.model.TAgreementOrderAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

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

    public boolean delete(int id) {
        delCache(id);
        return agreementOrderAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }
}
