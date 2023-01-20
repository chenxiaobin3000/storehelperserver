package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductOrderCommodityMapper;
import com.cxb.storehelperserver.model.TProductOrderCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 进货出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class ProductOrderCommodityRepository extends BaseRepository<TProductOrderCommodity> {
    @Resource
    private TProductOrderCommodityMapper productOrderCommodityMapper;

    public ProductOrderCommodityRepository() {
        init("poComm::");
    }

    public TProductOrderCommodity find(int id) {
        TProductOrderCommodity productOrderCommodity = getCache(id, TProductOrderCommodity.class);
        if (null != productOrderCommodity) {
            return productOrderCommodity;
        }

        // 缓存没有就查询数据库
        productOrderCommodity = productOrderCommodityMapper.selectByPrimaryKey(id);
        if (null != productOrderCommodity) {
            setCache(id, productOrderCommodity);
        }
        return productOrderCommodity;
    }

    public boolean insert(TProductOrderCommodity row) {
        if (productOrderCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TProductOrderCommodity row) {
        if (productOrderCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return productOrderCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
