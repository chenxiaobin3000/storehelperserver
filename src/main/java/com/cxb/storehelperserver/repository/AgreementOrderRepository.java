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
        init("aOrder::");
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

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TAgreementOrderExample example = new TAgreementOrderExample();
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
            return (int) agreementOrderMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TAgreementOrderExample example = new TAgreementOrderExample();
            example.or().andGidEqualTo(gid);
            total = (int) agreementOrderMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TAgreementOrder> pagination(int gid, int page, int limit, String search) {
        TAgreementOrderExample example = new TAgreementOrderExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return agreementOrderMapper.selectByExample(example);
    }

    public List<TAgreementOrder> getAllByDate(int gid, Date start, Date end) {
        TAgreementOrderExample example = new TAgreementOrderExample();
        example.or().andGidEqualTo(gid).andApplyTimeGreaterThanOrEqualTo(start).andApplyTimeLessThan(end).andReviewGreaterThan(0);
        return agreementOrderMapper.selectByExample(example);
    }

    public boolean check(int sid) {
        TAgreementOrderExample example = new TAgreementOrderExample();
        example.or().andSidEqualTo(sid);
        return null != agreementOrderMapper.selectOneByExample(example);
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
