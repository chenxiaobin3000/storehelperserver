package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductOrderMapper;
import com.cxb.storehelperserver.model.TProductOrder;
import com.cxb.storehelperserver.model.TProductOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

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

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TProductOrderExample example = new TProductOrderExample();
        example.or().andGidEqualTo(gid);
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
}
