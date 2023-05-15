package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementFareMapper;
import com.cxb.storehelperserver.model.TAgreementFare;
import com.cxb.storehelperserver.model.TAgreementFareExample;
import com.cxb.storehelperserver.repository.mapper.MyFareMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.cxb.storehelperserver.util.TypeDefine.ReviewType;
import com.cxb.storehelperserver.util.TypeDefine.CompleteType;

/**
 * desc: 履约物流费用仓库
 * auth: cxb
 * date: 2023/2/21
 */
@Slf4j
@Repository
public class AgreementFareRepository extends BaseRepository<List> {
    @Resource
    private TAgreementFareMapper agreementFareMapper;

    @Resource
    private MyFareMapper myFareMapper;

    public AgreementFareRepository() {
        init("agreeFare::");
    }

    public TAgreementFare find(int id) {
        return agreementFareMapper.selectByPrimaryKey(id);
    }

    public List<TAgreementFare> findByOid(int oid) {
        List<TAgreementFare> agreementFares = getCache(oid, List.class);
        if (null != agreementFares) {
            return agreementFares;
        }

        // 缓存没有就查询数据库
        TAgreementFareExample example = new TAgreementFareExample();
        example.or().andOidEqualTo(oid);
        agreementFares = agreementFareMapper.selectByExample(example);
        if (null != agreementFares) {
            setCache(oid, agreementFares);
        }
        return agreementFares;
    }

    public int total(int gid, int aid, int sid, int type, ReviewType review, CompleteType complete, Date start, Date end) {
        return myFareMapper.countAgreementOrder(gid, aid, sid, type, review.getValue(), complete.getValue(), start, end);
    }

    public List<TAgreementFare> pagination(int gid, int aid, int sid, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end) {
        return myFareMapper.paginationAgreementOrder((page - 1) * limit, limit, gid, aid, sid, type, review.getValue(), complete.getValue(), start, end);
    }

    public boolean insert(int oid, String ship, String code, String phone, BigDecimal fare, String remark, Date cdate) {
        TAgreementFare row = new TAgreementFare();
        row.setOid(oid);
        row.setShip(ship);
        row.setCode(code);
        row.setPhone(phone);
        row.setFare(fare);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (agreementFareMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementFare row) {
        if (agreementFareMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
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

    public boolean setReviewNull(int oid) {
        delCache(oid);
        return myFareMapper.setAgreementFareReviewNull(oid) > 0;
    }
}
