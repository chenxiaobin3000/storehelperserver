package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSaleOrderMapper;
import com.cxb.storehelperserver.model.TSaleOrder;
import com.cxb.storehelperserver.model.TSaleOrderExample;
import com.cxb.storehelperserver.repository.mapper.MyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 销售出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SaleOrderRepository extends BaseRepository<TSaleOrder> {
    @Resource
    private TSaleOrderMapper saleOrderMapper;

    @Resource
    private MyOrderMapper myOrderMapper;

    public SaleOrderRepository() {
        init("saleOrder::");
    }

    public TSaleOrder find(int id) {
        TSaleOrder saleOrder = getCache(id, TSaleOrder.class);
        if (null != saleOrder) {
            return saleOrder;
        }

        // 缓存没有就查询数据库
        saleOrder = saleOrderMapper.selectByPrimaryKey(id);
        if (null != saleOrder) {
            setCache(id, saleOrder);
        }
        return saleOrder;
    }

    public int total(int gid, int type, ReviewType review, String date) {
        TSaleOrderExample example = new TSaleOrderExample();
        TSaleOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
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
        return (int) saleOrderMapper.countByExample(example);
    }

    public List<TSaleOrder> pagination(int gid, int type, int page, int limit, ReviewType review, String date) {
        TSaleOrderExample example = new TSaleOrderExample();
        TSaleOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
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
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return saleOrderMapper.selectByExample(example);
    }

    public List<TSaleOrder> getAllByDate(int gid, Date start, Date end) {
        TSaleOrderExample example = new TSaleOrderExample();
        example.or().andGidEqualTo(gid).andApplyTimeGreaterThanOrEqualTo(start).andApplyTimeLessThan(end).andReviewGreaterThan(0);
        return saleOrderMapper.selectByExample(example);
    }

    public boolean check(int sid) {
        TSaleOrderExample example = new TSaleOrderExample();
        example.or().andSidEqualTo(sid);
        return null != saleOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TSaleOrder row) {
        if (saleOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSaleOrder row) {
        if (saleOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return saleOrderMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int id) {
        delCache(id);
        return myOrderMapper.setSaleReviewNull(id) > 0;
    }
}
