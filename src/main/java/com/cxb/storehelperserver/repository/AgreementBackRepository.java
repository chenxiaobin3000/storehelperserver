package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementBackMapper;
import com.cxb.storehelperserver.model.TAgreementBack;
import com.cxb.storehelperserver.model.TAgreementBackExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 线下退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class AgreementBackRepository extends BaseRepository<TAgreementBack> {
    @Resource
    private TAgreementBackMapper agreementBackMapper;

    public AgreementBackRepository() {
        init("agreeBack::");
    }

    public TAgreementBack find(int oid) {
        TAgreementBack agreementBack = getCache(oid, TAgreementBack.class);
        if (null != agreementBack) {
            return agreementBack;
        }

        // 缓存没有就查询数据库
        TAgreementBackExample example = new TAgreementBackExample();
        example.or().andOidEqualTo(oid);
        agreementBack = agreementBackMapper.selectOneByExample(example);
        if (null != agreementBack) {
            setCache(oid, agreementBack);
        }
        return agreementBack;
    }

    public List<TAgreementBack> findByPid(int pid) {
        TAgreementBackExample example = new TAgreementBackExample();
        example.or().andPidEqualTo(pid);
        return agreementBackMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TAgreementBackExample example = new TAgreementBackExample();
        example.or().andPidEqualTo(pid);
        return null != agreementBackMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int pid) {
        TAgreementBack row = new TAgreementBack();
        row.setOid(oid);
        row.setPid(pid);
        if (agreementBackMapper.insert(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementBack row) {
        if (agreementBackMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TAgreementBack agreementBack = agreementBackMapper.selectByPrimaryKey(id);
        if (null == agreementBack) {
            return false;
        }
        delCache(agreementBack.getOid());
        return agreementBackMapper.deleteByPrimaryKey(id) > 0;
    }
}
