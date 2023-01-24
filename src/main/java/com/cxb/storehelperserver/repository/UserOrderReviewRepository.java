package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserOrderReviewMapper;
import com.cxb.storehelperserver.model.TUserOrderReview;
import com.cxb.storehelperserver.model.TUserOrderReviewExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 用户订单审核仓库
 * auth: cxb
 * date: 2023/1/23
 */
@Slf4j
@Repository
public class UserOrderReviewRepository extends BaseRepository<TUserOrderReview> {
    @Resource
    private TUserOrderReviewMapper userOrderReviewMapper;

    public UserOrderReviewRepository() {
        init("userOR::");
    }

    public TUserOrderReview find(int otype, int oid) {
        TUserOrderReview userOrderReview = getCache(joinKey(otype, oid), TUserOrderReview.class);
        if (null != userOrderReview) {
            return userOrderReview;
        }

        // 缓存没有就查询数据库
        TUserOrderReviewExample example = new TUserOrderReviewExample();
        example.or().andOtypeEqualTo(otype).andOidEqualTo(oid);
        userOrderReview = userOrderReviewMapper.selectOneByExample(example);
        if (null != userOrderReview) {
            setCache(joinKey(otype, oid), userOrderReview);
        }
        return userOrderReview;
    }

    public int total(int uid, String search) {
        TUserOrderReviewExample example = new TUserOrderReviewExample();
        if (null != search) {
            example.or().andUidEqualTo(uid).andBatchLike("%" + search + "%");
        } else {
            example.or().andUidEqualTo(uid);
        }
        return (int) userOrderReviewMapper.countByExample(example);
    }

    public List<TUserOrderReview> pagination(int uid, int page, int limit, String search) {
        TUserOrderReviewExample example = new TUserOrderReviewExample();
        if (null != search) {
            example.or().andUidEqualTo(uid).andBatchLike("%" + search + "%");
        } else {
            example.or().andUidEqualTo(uid);
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return userOrderReviewMapper.selectByExample(example);
    }

    public boolean insert(TUserOrderReview row) {
        if (userOrderReviewMapper.insert(row) > 0) {
            setCache(joinKey(row.getOtype(), row.getOid()), row);
            return true;
        }
        return false;
    }

    public boolean update(TUserOrderReview row) {
        if (userOrderReviewMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getOtype(), row.getOid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int otype, int oid) {
        delCache(joinKey(otype, oid));
        TUserOrderReviewExample example = new TUserOrderReviewExample();
        example.or().andOtypeEqualTo(otype).andOidEqualTo(oid);
        return userOrderReviewMapper.deleteByExample(example) > 0;
    }
}
