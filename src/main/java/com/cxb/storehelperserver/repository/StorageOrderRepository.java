package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageOrderMapper;
import com.cxb.storehelperserver.model.TStorageOrder;
import com.cxb.storehelperserver.model.TStorageOrderExample;
import com.cxb.storehelperserver.repository.mapper.MyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 进货出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class StorageOrderRepository extends BaseRepository<TStorageOrder> {
    @Resource
    private TStorageOrderMapper storageOrderMapper;

    @Resource
    private MyOrderMapper myOrderMapper;

    public StorageOrderRepository() {
        init("storageOrder::");
    }

    public TStorageOrder find(int id) {
        TStorageOrder storageOrder = getCache(id, TStorageOrder.class);
        if (null != storageOrder) {
            return storageOrder;
        }

        // 缓存没有就查询数据库
        storageOrder = storageOrderMapper.selectByPrimaryKey(id);
        if (null != storageOrder) {
            setCache(id, storageOrder);
        }
        return storageOrder;
    }

    public int total(int gid, int type, ReviewType review, String search) {
        // 包含搜索的不缓存
        TStorageOrderExample example = new TStorageOrderExample();
        TStorageOrderExample.Criteria criteria = example.createCriteria();
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
        return (int) storageOrderMapper.countByExample(example);
    }

    public List<TStorageOrder> pagination(int gid, int type, int page, int limit, ReviewType review, String search) {
        TStorageOrderExample example = new TStorageOrderExample();
        TStorageOrderExample.Criteria criteria = example.createCriteria();
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
        return storageOrderMapper.selectByExample(example);
    }

    public List<TStorageOrder> getAllByDate(int gid, Date start, Date end) {
        TStorageOrderExample example = new TStorageOrderExample();
        example.or().andGidEqualTo(gid).andApplyTimeGreaterThanOrEqualTo(start).andApplyTimeLessThan(end).andReviewGreaterThan(0);
        return storageOrderMapper.selectByExample(example);
    }

    public boolean check(int sid) {
        TStorageOrderExample example = new TStorageOrderExample();
        example.or().andSidEqualTo(sid);
        return null != storageOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TStorageOrder row) {
        if (storageOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageOrder row) {
        if (storageOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return storageOrderMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int id) {
        delCache(id);
        return myOrderMapper.setStorageReviewNull(id) > 0;
    }
}
