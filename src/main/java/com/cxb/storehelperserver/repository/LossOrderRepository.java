package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TLossOrderMapper;
import com.cxb.storehelperserver.model.TLossOrder;
import com.cxb.storehelperserver.model.TLossOrderExample;
import com.cxb.storehelperserver.repository.mapper.MyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 损耗出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class LossOrderRepository extends BaseRepository<TLossOrder> {
    @Resource
    private TLossOrderMapper lossOrderMapper;

    @Resource
    private MyOrderMapper myOrderMapper;

    public LossOrderRepository() {
        init("lossOrder::");
    }

    public TLossOrder find(int id) {
        TLossOrder lossOrder = getCache(id, TLossOrder.class);
        if (null != lossOrder) {
            return lossOrder;
        }

        // 缓存没有就查询数据库
        lossOrder = lossOrderMapper.selectByPrimaryKey(id);
        if (null != lossOrder) {
            setCache(id, lossOrder);
        }
        return lossOrder;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TLossOrderExample example = new TLossOrderExample();
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
            return (int) lossOrderMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TLossOrderExample example = new TLossOrderExample();
            example.or().andGidEqualTo(gid);
            total = (int) lossOrderMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TLossOrder> pagination(int gid, int page, int limit, String search) {
        TLossOrderExample example = new TLossOrderExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return lossOrderMapper.selectByExample(example);
    }

    public List<TLossOrder> getAllByDate(int gid, Date start, Date end) {
        TLossOrderExample example = new TLossOrderExample();
        example.or().andGidEqualTo(gid).andApplyTimeGreaterThanOrEqualTo(start).andApplyTimeLessThan(end).andReviewGreaterThan(0);
        return lossOrderMapper.selectByExample(example);
    }

    public boolean check(int sid) {
        TLossOrderExample example = new TLossOrderExample();
        example.or().andSidEqualTo(sid);
        return null != lossOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TLossOrder row) {
        if (lossOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TLossOrder row) {
        if (lossOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return lossOrderMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int id) {
        delCache(id);
        return myOrderMapper.setLossReviewNull(id) > 0;
    }
}
