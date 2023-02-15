package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseCommodityMapper;
import com.cxb.storehelperserver.model.TPurchaseCommodity;
import com.cxb.storehelperserver.model.TPurchaseCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyPurchaseCommodityMapper;
import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 采购出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class PurchaseCommodityRepository extends BaseRepository<List> {
    @Resource
    private TPurchaseCommodityMapper purchaseCommodityMapper;

    @Resource
    private MyPurchaseCommodityMapper myPurchaseCommodityMapper;

    public PurchaseCommodityRepository() {
        init("purchaseComm::");
    }

    public List<TPurchaseCommodity> find(int oid) {
        List<TPurchaseCommodity> purchaseCommoditys = getCache(oid, List.class);
        if (null != purchaseCommoditys) {
            return purchaseCommoditys;
        }

        // 缓存没有就查询数据库
        TPurchaseCommodityExample example = new TPurchaseCommodityExample();
        example.or().andOidEqualTo(oid);
        purchaseCommoditys = purchaseCommodityMapper.selectByExample(example);
        if (null != purchaseCommoditys) {
            setCache(oid, purchaseCommoditys);
        }
        return purchaseCommoditys;
    }

    public List<MyOrderCommodity> findByGid(int gid, Date start, Date end) {
        return myPurchaseCommodityMapper.selectByGid(gid, start, end);
    }

    public List<MyOrderCommodity> findBySid(int sid, Date start, Date end) {
        return myPurchaseCommodityMapper.selectBySid(sid, start, end);
    }

    // 注意：数据被缓存在PurchaseCommodityService，所以不能直接调用该函数
    public boolean update(List<TPurchaseCommodity> rows, int oid) {
        delete(oid);
        for (TPurchaseCommodity purchaseCommodity : rows) {
            if (purchaseCommodityMapper.insert(purchaseCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TPurchaseCommodityExample example = new TPurchaseCommodityExample();
        example.or().andOidEqualTo(oid);
        return purchaseCommodityMapper.deleteByExample(example) > 0;
    }
}
