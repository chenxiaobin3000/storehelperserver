package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseOrderMapper;
import com.cxb.storehelperserver.model.TPurchaseOrder;
import com.cxb.storehelperserver.model.TPurchaseOrderExample;
import com.cxb.storehelperserver.repository.mapper.MyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 采购出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class PurchaseOrderRepository extends BaseRepository<TPurchaseOrder> {
    @Resource
    private TPurchaseOrderMapper purchaseOrderMapper;

    @Resource
    private MyOrderMapper myOrderMapper;

    public PurchaseOrderRepository() {
        init("purOrder::");
    }

    public TPurchaseOrder find(int id) {
        TPurchaseOrder purchaseOrder = getCache(id, TPurchaseOrder.class);
        if (null != purchaseOrder) {
            return purchaseOrder;
        }

        // 缓存没有就查询数据库
        purchaseOrder = purchaseOrderMapper.selectByPrimaryKey(id);
        if (null != purchaseOrder) {
            setCache(id, purchaseOrder);
        }
        return purchaseOrder;
    }

    public int total(int gid, int type, ReviewType review, CompleteType complete, String date) {
        TPurchaseOrderExample example = new TPurchaseOrderExample();
        TPurchaseOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
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
        return (int) purchaseOrderMapper.countByExample(example);
    }

    public List<TPurchaseOrder> pagination(int gid, int type, int page, int limit, ReviewType review, CompleteType complete, String date) {
        TPurchaseOrderExample example = new TPurchaseOrderExample();
        TPurchaseOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
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
        return purchaseOrderMapper.selectByExample(example);
    }

    public List<TPurchaseOrder> getAllByDate(int gid, Date start, Date end) {
        TPurchaseOrderExample example = new TPurchaseOrderExample();
        example.or().andGidEqualTo(gid).andApplyTimeGreaterThanOrEqualTo(start).andApplyTimeLessThan(end).andReviewGreaterThan(0);
        return purchaseOrderMapper.selectByExample(example);
    }

    public boolean check(int sid) {
        TPurchaseOrderExample example = new TPurchaseOrderExample();
        example.or().andSidEqualTo(sid);
        return null != purchaseOrderMapper.selectOneByExample(example);
    }

    public boolean checkBySupplier(int sid) {
        TPurchaseOrderExample example = new TPurchaseOrderExample();
        example.or().andSupplierEqualTo(sid);
        return null != purchaseOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TPurchaseOrder row) {
        if (purchaseOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TPurchaseOrder row) {
        if (purchaseOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return purchaseOrderMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int id) {
        delCache(id);
        return myOrderMapper.setPurchaseReviewNull(id) > 0;
    }
}
