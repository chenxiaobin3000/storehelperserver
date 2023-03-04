package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementRemarkMapper;
import com.cxb.storehelperserver.model.TAgreementRemark;
import com.cxb.storehelperserver.model.TAgreementRemarkExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 履约备注关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class AgreementRemarkRepository extends BaseRepository<List> {
    @Resource
    private TAgreementRemarkMapper agreementRemarkMapper;

    public AgreementRemarkRepository() {
        init("agreementRemark::");
    }

    public TAgreementRemark find(int id) {
        return agreementRemarkMapper.selectByPrimaryKey(id);
    }

    public List<TAgreementRemark> findByOid(int oid) {
        List<TAgreementRemark> agreementRemarks = getCache(oid, List.class);
        if (null != agreementRemarks) {
            return agreementRemarks;
        }

        // 缓存没有就查询数据库
        TAgreementRemarkExample example = new TAgreementRemarkExample();
        example.or().andOidEqualTo(oid);
        agreementRemarks = agreementRemarkMapper.selectByExample(example);
        if (null != agreementRemarks) {
            setCache(oid, agreementRemarks);
        }
        return agreementRemarks;
    }

    public boolean insert(int oid, String remark, Date cdate) {
        TAgreementRemark row = new TAgreementRemark();
        row.setOid(oid);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (agreementRemarkMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementRemark row) {
        if (agreementRemarkMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TAgreementRemark agreementRemark = agreementRemarkMapper.selectByPrimaryKey(id);
        if (null == agreementRemark) {
            return false;
        }
        delCache(agreementRemark.getOid());
        return agreementRemarkMapper.deleteByPrimaryKey(id) > 0;
    }
}
