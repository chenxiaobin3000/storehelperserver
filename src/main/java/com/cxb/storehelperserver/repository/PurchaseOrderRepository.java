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

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TPurchaseOrderExample example = new TPurchaseOrderExample();
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
            return (int) purchaseOrderMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TPurchaseOrderExample example = new TPurchaseOrderExample();
            example.or().andGidEqualTo(gid);
            total = (int) purchaseOrderMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TPurchaseOrder> pagination(int gid, int page, int limit, String search) {
        TPurchaseOrderExample example = new TPurchaseOrderExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
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
