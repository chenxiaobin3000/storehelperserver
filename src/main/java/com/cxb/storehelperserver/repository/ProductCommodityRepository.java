package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductCommodityMapper;
import com.cxb.storehelperserver.model.TProductCommodity;
import com.cxb.storehelperserver.model.TProductCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyProductCommodityMapper;
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
public class ProductCommodityRepository extends BaseRepository<List> {
    @Resource
    private TProductCommodityMapper productCommodityMapper;

    @Resource
    private MyProductCommodityMapper myProductCommodityMapper;

    public ProductCommodityRepository() {
        init("productComm::");
    }

    public List<TProductCommodity> find(int oid) {
        List<TProductCommodity> productCommoditys = getCache(oid, List.class);
        if (null != productCommoditys) {
            return productCommoditys;
        }

        // 缓存没有就查询数据库
        TProductCommodityExample example = new TProductCommodityExample();
        example.or().andOidEqualTo(oid);
        productCommoditys = productCommodityMapper.selectByExample(example);
        if (null != productCommoditys) {
            setCache(oid, productCommoditys);
        }
        return productCommoditys;
    }

    public List<MyOrderCommodity> findByGid(int gid, Date start, Date end) {
        return myProductCommodityMapper.selectByGid(gid, start, end);
    }

    public List<MyOrderCommodity> findBySid(int sid, Date start, Date end) {
        return myProductCommodityMapper.selectBySid(sid, start, end);
    }

    // 注意：数据被缓存在ProductCommodityService，所以不能直接调用该函数
    public boolean update(List<TProductCommodity> rows, int oid) {
        delete(oid);
        for (TProductCommodity productCommodity : rows) {
            if (productCommodityMapper.insert(productCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TProductCommodityExample example = new TProductCommodityExample();
        example.or().andOidEqualTo(oid);
        return productCommodityMapper.deleteByExample(example) > 0;
    }
}
