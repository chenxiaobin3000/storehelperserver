package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementFareMapper;
import com.cxb.storehelperserver.model.TAgreementFare;
import com.cxb.storehelperserver.model.TAgreementFareExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * desc: 履约物流费用仓库
 * auth: cxb
 * date: 2023/2/21
 */
@Slf4j
@Repository
public class AgreementFareRepository extends BaseRepository<TAgreementFare> {
    @Resource
    private TAgreementFareMapper agreementFareMapper;

    public AgreementFareRepository() {
        init("agreeFare::");
    }

    public TAgreementFare find(int oid) {
        TAgreementFare agreementFare = getCache(oid, TAgreementFare.class);
        if (null != agreementFare) {
            return agreementFare;
        }

        // 缓存没有就查询数据库
        TAgreementFareExample example = new TAgreementFareExample();
        example.or().andOidEqualTo(oid);
        agreementFare = agreementFareMapper.selectOneByExample(example);
        if (null != agreementFare) {
            setCache(oid, agreementFare);
        }
        return agreementFare;
    }

    public boolean insert(int oid, BigDecimal fare) {
        TAgreementFare row = new TAgreementFare();
        row.setOid(oid);
        row.setFare(fare);
        if (agreementFareMapper.insert(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementFare row) {
        if (agreementFareMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TAgreementFare agreementFare = agreementFareMapper.selectByPrimaryKey(id);
        if (null == agreementFare) {
            return false;
        }
        delCache(agreementFare.getOid());
        return agreementFareMapper.deleteByPrimaryKey(id) > 0;
    }
}
