package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudOrderMapper;
import com.cxb.storehelperserver.model.TCloudOrder;
import com.cxb.storehelperserver.model.TCloudOrderExample;
import com.cxb.storehelperserver.repository.mapper.MyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 云仓出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class CloudOrderRepository extends BaseRepository<TCloudOrder> {
    @Resource
    private TCloudOrderMapper cloudOrderMapper;

    @Resource
    private MyOrderMapper myOrderMapper;

    public CloudOrderRepository() {
        init("cloudOrder::");
    }

    public TCloudOrder find(int id) {
        TCloudOrder cloudOrder = getCache(id, TCloudOrder.class);
        if (null != cloudOrder) {
            return cloudOrder;
        }

        // 缓存没有就查询数据库
        cloudOrder = cloudOrderMapper.selectByPrimaryKey(id);
        if (null != cloudOrder) {
            setCache(id, cloudOrder);
        }
        return cloudOrder;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TCloudOrderExample example = new TCloudOrderExample();
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
            return (int) cloudOrderMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TCloudOrderExample example = new TCloudOrderExample();
            example.or().andGidEqualTo(gid);
            total = (int) cloudOrderMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TCloudOrder> pagination(int gid, int page, int limit, String search) {
        TCloudOrderExample example = new TCloudOrderExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return cloudOrderMapper.selectByExample(example);
    }

    public List<TCloudOrder> getAllByDate(int gid, Date start, Date end) {
        TCloudOrderExample example = new TCloudOrderExample();
        example.or().andGidEqualTo(gid).andApplyTimeGreaterThanOrEqualTo(start).andApplyTimeLessThan(end).andReviewGreaterThan(0);
        return cloudOrderMapper.selectByExample(example);
    }

    public boolean check(int sid) {
        TCloudOrderExample example = new TCloudOrderExample();
        example.or().andSidEqualTo(sid);
        return null != cloudOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TCloudOrder row) {
        if (cloudOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TCloudOrder row) {
        if (cloudOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return cloudOrderMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int id) {
        delCache(id);
        return myOrderMapper.setCloudReviewNull(id) > 0;
    }
}
