package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOrderReviewerMapper;
import com.cxb.storehelperserver.model.TOrderReviewer;
import com.cxb.storehelperserver.model.TOrderReviewerExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 订单审核权限人仓库
 * auth: cxb
 * date: 2023/1/23
 */
@Slf4j
@Repository
public class OrderReviewerRepository extends BaseRepository<List> {
    @Resource
    private TOrderReviewerMapper orderReviewerMapper;

    public OrderReviewerRepository() {
        init("orderRe::");
    }

    public List<TOrderReviewer> find(int gid) {
        List<TOrderReviewer> orderReviewers = getCache(gid, List.class);
        if (null != orderReviewers) {
            return orderReviewers;
        }

        // 缓存没有就查询数据库
        TOrderReviewerExample example = new TOrderReviewerExample();
        example.or().andGidEqualTo(gid);
        orderReviewers = orderReviewerMapper.selectByExample(example);
        if (null != orderReviewers) {
            setCache(gid, orderReviewers);
        }
        return orderReviewers;
    }

    public boolean update(List<TOrderReviewer> rows, int gid) {
        delete(gid);
        for (TOrderReviewer orderReviewer : rows) {
            if (orderReviewerMapper.insert(orderReviewer) < 1) {
                return false;
            }
        }
        setCache(gid, rows);
        return true;
    }

    public boolean delete(int gid) {
        delCache(gid);
        TOrderReviewerExample example = new TOrderReviewerExample();
        example.or().andGidEqualTo(gid);
        return orderReviewerMapper.deleteByExample(example) > 0;
    }
}
