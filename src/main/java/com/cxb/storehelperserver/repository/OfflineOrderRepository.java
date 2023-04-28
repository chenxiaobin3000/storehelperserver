package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOfflineOrderMapper;
import com.cxb.storehelperserver.model.TOfflineOrder;
import com.cxb.storehelperserver.model.TOfflineOrderExample;
import com.cxb.storehelperserver.repository.mapper.MyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 线下销售订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class OfflineOrderRepository extends BaseRepository<TOfflineOrder> {
    @Resource
    private TOfflineOrderMapper offlineOrderMapper;

    @Resource
    private MyOrderMapper myOrderMapper;

    public OfflineOrderRepository() {
        init("offlineOrder::");
    }

    public TOfflineOrder find(int id) {
        TOfflineOrder offlineOrder = getCache(id, TOfflineOrder.class);
        if (null != offlineOrder) {
            return offlineOrder;
        }

        // 缓存没有就查询数据库
        offlineOrder = offlineOrderMapper.selectByPrimaryKey(id);
        if (null != offlineOrder) {
            setCache(id, offlineOrder);
        }
        return offlineOrder;
    }

    public List<TOfflineOrder> findByAid(int aid, int asid) {
        TOfflineOrderExample example = new TOfflineOrderExample();
        if (0 == asid) {
            example.or().andAidEqualTo(aid);
        } else {
            example.or().andAidEqualTo(aid).andAsidEqualTo(asid);
        }
        return offlineOrderMapper.selectByExample(example);
    }

    public int total(int gid, int aid, int asid, int type, ReviewType review, String date) {
        TOfflineOrderExample example = new TOfflineOrderExample();
        TOfflineOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
        if (aid > 0) {
            criteria.andAidEqualTo(aid);
        }
        if (asid > 0) {
            criteria.andAsidEqualTo(asid);
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
        return (int) offlineOrderMapper.countByExample(example);
    }

    public List<TOfflineOrder> pagination(int gid, int aid, int asid, int type, int page, int limit, ReviewType review, String date) {
        TOfflineOrderExample example = new TOfflineOrderExample();
        TOfflineOrderExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid).andOtypeEqualTo(type);
        if (aid > 0) {
            criteria.andAidEqualTo(aid);
        }
        if (asid > 0) {
            criteria.andAsidEqualTo(asid);
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
        return offlineOrderMapper.selectByExample(example);
    }

    public List<TOfflineOrder> getAllByDate(int gid, Date start, Date end) {
        TOfflineOrderExample example = new TOfflineOrderExample();
        example.or().andGidEqualTo(gid).andApplyTimeGreaterThanOrEqualTo(start).andApplyTimeLessThan(end).andReviewGreaterThan(0);
        return offlineOrderMapper.selectByExample(example);
    }

    public boolean check(int aid, int asid) {
        TOfflineOrderExample example = new TOfflineOrderExample();
        example.or().andAidEqualTo(aid).andAsidEqualTo(asid);
        return null != offlineOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TOfflineOrder row) {
        if (offlineOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TOfflineOrder row) {
        if (offlineOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return offlineOrderMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int id) {
        delCache(id);
        return myOrderMapper.setOfflineReviewNull(id) > 0;
    }
}
