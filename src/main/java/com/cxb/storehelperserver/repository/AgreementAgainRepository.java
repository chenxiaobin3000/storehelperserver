package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementAgainMapper;
import com.cxb.storehelperserver.model.TAgreementAgain;
import com.cxb.storehelperserver.model.TAgreementAgainExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 履约退转入关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class AgreementAgainRepository extends BaseRepository<TAgreementAgain> {
    @Resource
    private TAgreementAgainMapper agreementAgainMapper;

    public AgreementAgainRepository() {
        init("agreeAgain::");
    }

    public TAgreementAgain find(int oid) {
        TAgreementAgain agreementAgain = getCache(oid, TAgreementAgain.class);
        if (null != agreementAgain) {
            return agreementAgain;
        }

        // 缓存没有就查询数据库
        TAgreementAgainExample example = new TAgreementAgainExample();
        example.or().andOidEqualTo(oid);
        agreementAgain = agreementAgainMapper.selectOneByExample(example);
        if (null != agreementAgain) {
            setCache(oid, agreementAgain);
        }
        return agreementAgain;
    }

    public List<TAgreementAgain> findByAid(int aid) {
        TAgreementAgainExample example = new TAgreementAgainExample();
        example.or().andAidEqualTo(aid);
        return agreementAgainMapper.selectByExample(example);
    }

    public boolean checkByAid(int aid) {
        TAgreementAgainExample example = new TAgreementAgainExample();
        example.or().andAidEqualTo(aid);
        return null != agreementAgainMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int aid) {
        TAgreementAgain row = new TAgreementAgain();
        row.setOid(oid);
        row.setAid(aid);
        if (agreementAgainMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementAgain row) {
        if (agreementAgainMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int aid) {
        delCache(oid);
        TAgreementAgainExample example = new TAgreementAgainExample();
        example.or().andOidEqualTo(oid).andAidEqualTo(aid);
        return agreementAgainMapper.deleteByExample(example) > 0;
    }
}
