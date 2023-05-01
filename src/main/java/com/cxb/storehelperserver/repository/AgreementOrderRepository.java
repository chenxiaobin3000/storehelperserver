package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementOrderMapper;
import com.cxb.storehelperserver.model.TAgreementOrder;
import com.cxb.storehelperserver.model.TAgreementOrderExample;
import com.cxb.storehelperserver.repository.mapper.MyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 履约出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class AgreementOrderRepository extends BaseRepository<TAgreementOrder> {
    @Resource
    private TAgreementOrderMapper agreementOrderMapper;

    @Resource
    private MyOrderMapper myOrderMapper;

    public AgreementOrderRepository() {
        init("agreeOrder::");
    }

    public TAgreementOrder find(int id) {
        TAgreementOrder agreementOrder = getCache(id, TAgreementOrder.class);
        if (null != agreementOrder) {
            return agreementOrder;
        }

        // 缓存没有就查询数据库
        agreementOrder = agreementOrderMapper.selectByPrimaryKey(id);
        if (null != agreementOrder) {
            setCache(id, agreementOrder);
        }
        return agreementOrder;
    }

    public List<TAgreementOrder> findByAid(int aid, Byte complete) {
        TAgreementOrderExample example = new TAgreementOrderExample();
        example.or().andAidEqualTo(aid).andCompleteEqualTo(complete);
        return agreementOrderMapper.selectByExample(example);
    }

    public int total(int gid, int aid, int type, ReviewType review, CompleteType complete, String date) {
        TAgreementOrderExample example = new TAgreementOrderExample();
        TAgreementOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
        if (aid > 0) {
            criteria.andAidEqualTo(aid);
        }
        if (null != date) {
            criteria.andBatchLike("%" + date + "%");
        }
        switch (review) {
            case REVIEW_HAS:
                criteria.andReviewIsNotNull();
                break;
            case REVIEW_NOT:
                criteria.andReviewIsNull();
                break;
            default:
                break;
        }
        switch (complete) {
            case COMPLETE_HAS:
                criteria.andCompleteEqualTo(new Byte("1"));
                break;
            case COMPLETE_NOT:
                criteria.andCompleteEqualTo(new Byte("0"));
                break;
            default:
                break;
        }
        return (int) agreementOrderMapper.countByExample(example);
    }

    public List<TAgreementOrder> pagination(int gid, int aid, int type, int page, int limit, ReviewType review, CompleteType complete, String date) {
        TAgreementOrderExample example = new TAgreementOrderExample();
        TAgreementOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
        if (aid > 0) {
            criteria.andAidEqualTo(aid);
        }
        if (null != date) {
            criteria.andBatchLike("%" + date + "%");
        }
        switch (review) {
            case REVIEW_HAS:
                criteria.andReviewIsNotNull();
                break;
            case REVIEW_NOT:
                criteria.andReviewIsNull();
                break;
            default:
                break;
        }
        switch (complete) {
            case COMPLETE_HAS:
                criteria.andCompleteEqualTo(new Byte("1"));
                break;
            case COMPLETE_NOT:
                criteria.andCompleteEqualTo(new Byte("0"));
                break;
            default:
                break;
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return agreementOrderMapper.selectByExample(example);
    }

    public List<TAgreementOrder> getAllByDate(int gid, Date start, Date end) {
        TAgreementOrderExample example = new TAgreementOrderExample();
        example.or().andGidEqualTo(gid).andApplyTimeGreaterThanOrEqualTo(start).andApplyTimeLessThan(end).andReviewGreaterThan(0);
        return agreementOrderMapper.selectByExample(example);
    }

    public boolean insert(TAgreementOrder row) {
        if (agreementOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementOrder row) {
        if (agreementOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return agreementOrderMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int id) {
        delCache(id);
        return myOrderMapper.setAgreementReviewNull(id) > 0;
    }
}
