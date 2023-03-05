package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductOrderMapper;
import com.cxb.storehelperserver.model.TProductOrder;
import com.cxb.storehelperserver.model.TProductOrderExample;
import com.cxb.storehelperserver.repository.mapper.MyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 生产出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class ProductOrderRepository extends BaseRepository<TProductOrder> {
    @Resource
    private TProductOrderMapper productOrderMapper;

    @Resource
    private MyOrderMapper myOrderMapper;

    public ProductOrderRepository() {
        init("productOrder::");
    }

    public TProductOrder find(int id) {
        TProductOrder productOrder = getCache(id, TProductOrder.class);
        if (null != productOrder) {
            return productOrder;
        }

        // 缓存没有就查询数据库
        productOrder = productOrderMapper.selectByPrimaryKey(id);
        if (null != productOrder) {
            setCache(id, productOrder);
        }
        return productOrder;
    }

    public int total(int gid, int type, ReviewType review, String search) {
        TProductOrderExample example = new TProductOrderExample();
        TProductOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
        if (null != search) {
            criteria.andBatchLike("%" + search + "%");
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
        return (int) productOrderMapper.countByExample(example);
    }

    public List<TProductOrder> pagination(int gid, int type, int page, int limit, ReviewType review, String search) {
        TProductOrderExample example = new TProductOrderExample();
        TProductOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
        if (null != search) {
            criteria.andBatchLike("%" + search + "%");
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
        return productOrderMapper.selectByExample(example);
    }

    public List<TProductOrder> getAllByDate(int gid, Date start, Date end) {
        TProductOrderExample example = new TProductOrderExample();
        example.or().andGidEqualTo(gid).andApplyTimeGreaterThanOrEqualTo(start).andApplyTimeLessThan(end).andReviewGreaterThan(0);
        return productOrderMapper.selectByExample(example);
    }

    public boolean check(int sid) {
        TProductOrderExample example = new TProductOrderExample();
        example.or().andSidEqualTo(sid);
        return null != productOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TProductOrder row) {
        if (productOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TProductOrder row) {
        if (productOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return productOrderMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int id) {
        delCache(id);
        return myOrderMapper.setProductReviewNull(id) > 0;
    }
}
