package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductOrderMapper;
import com.cxb.storehelperserver.model.TProductOrder;
import com.cxb.storehelperserver.model.TProductOrderExample;
import com.cxb.storehelperserver.repository.mapper.MyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

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
        init("pOrder::");
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

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TProductOrderExample example = new TProductOrderExample();
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
            return (int) productOrderMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TProductOrderExample example = new TProductOrderExample();
            example.or().andGidEqualTo(gid);
            total = (int) productOrderMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TProductOrder> pagination(int gid, int page, int limit, String search) {
        TProductOrderExample example = new TProductOrderExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
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
