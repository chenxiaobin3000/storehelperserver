package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementReturnMapper;
import com.cxb.storehelperserver.model.TAgreementReturn;
import com.cxb.storehelperserver.model.TAgreementReturnExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 履约退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class AgreementReturnRepository extends BaseRepository<TAgreementReturn> {
    @Resource
    private TAgreementReturnMapper agreementReturnMapper;

    public AgreementReturnRepository() {
        init("agreeReturn::");
    }

    public TAgreementReturn find(int oid) {
        TAgreementReturn agreementReturn = getCache(oid, TAgreementReturn.class);
        if (null != agreementReturn) {
            return agreementReturn;
        }

        // 缓存没有就查询数据库
        TAgreementReturnExample example = new TAgreementReturnExample();
        example.or().andOidEqualTo(oid);
        agreementReturn = agreementReturnMapper.selectOneByExample(example);
        if (null != agreementReturn) {
            setCache(oid, agreementReturn);
        }
        return agreementReturn;
    }

    public List<TAgreementReturn> findByAid(int aid) {
        TAgreementReturnExample example = new TAgreementReturnExample();
        example.or().andAidEqualTo(aid);
        return agreementReturnMapper.selectByExample(example);
    }

    public boolean checkByAid(int aid) {
        TAgreementReturnExample example = new TAgreementReturnExample();
        example.or().andAidEqualTo(aid);
        return null != agreementReturnMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int aid) {
        TAgreementReturn row = new TAgreementReturn();
        row.setOid(oid);
        row.setAid(aid);
        if (agreementReturnMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementReturn row) {
        if (agreementReturnMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int aid) {
        delCache(oid);
        TAgreementReturnExample example = new TAgreementReturnExample();
        example.or().andOidEqualTo(oid).andAidEqualTo(aid);
        return agreementReturnMapper.deleteByExample(example) > 0;
    }
}
