package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductOrderCommodityMapper;
import com.cxb.storehelperserver.model.TProductOrderCommodity;
import com.cxb.storehelperserver.model.TProductOrderCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyProductOrderCommodityMapper;
import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 进货出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class ProductOrderCommodityRepository extends BaseRepository<List> {
    @Resource
    private TProductOrderCommodityMapper productOrderCommodityMapper;

    @Resource
    private MyProductOrderCommodityMapper myProductOrderCommodityMapper;

    public ProductOrderCommodityRepository() {
        init("poComm::");
    }

    public List<TProductOrderCommodity> find(int oid) {
        List<TProductOrderCommodity> productOrderCommoditys = getCache(oid, List.class);
        if (null != productOrderCommoditys) {
            return productOrderCommoditys;
        }

        // 缓存没有就查询数据库
        TProductOrderCommodityExample example = new TProductOrderCommodityExample();
        example.or().andOidEqualTo(oid);
        productOrderCommoditys = productOrderCommodityMapper.selectByExample(example);
        if (null != productOrderCommoditys) {
            setCache(oid, productOrderCommoditys);
        }
        return productOrderCommoditys;
    }

    public List<MyOrderCommodity> findBySid(int sid, Date start, Date end) {
        return myProductOrderCommodityMapper.select(sid, start, end);
    }

    // 注意：数据被缓存在ProductCommodityService，所以不能直接调用该函数
    public boolean update(List<TProductOrderCommodity> rows, int oid) {
        delete(oid);
        for (TProductOrderCommodity productOrderCommodity : rows) {
            if (productOrderCommodityMapper.insert(productOrderCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TProductOrderCommodityExample example = new TProductOrderCommodityExample();
        example.or().andOidEqualTo(oid);
        return productOrderCommodityMapper.deleteByExample(example) > 0;
    }
}
