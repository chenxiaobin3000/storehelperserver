package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseCommodityMapper;
import com.cxb.storehelperserver.model.TPurchaseCommodity;
import com.cxb.storehelperserver.model.TPurchaseCommodityExample;
import com.cxb.storehelperserver.model.TPurchaseOrder;
import com.cxb.storehelperserver.repository.mapper.MyCommodityCountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

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
    private MyCommodityCountMapper myCommodityCountMapper;

    public PurchaseCommodityRepository() {
        init("purComm::");
    }

    public TPurchaseCommodity findOne(int oid, int ctype, int cid) {
        List<TPurchaseCommodity> purchaseCommoditys = getCache(oid, List.class);
        if (null != purchaseCommoditys) {
            for (TPurchaseCommodity c : purchaseCommoditys) {
                if (c.getCtype() == ctype && c.getCid() == cid) {
                    return c;
                }
            }
        }

        // 缓存没有就查询数据库
        TPurchaseCommodityExample example = new TPurchaseCommodityExample();
        example.or().andOidEqualTo(oid).andCtypeEqualTo(ctype).andCidEqualTo(cid);
        return purchaseCommodityMapper.selectOneByExample(example);
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

    public BigDecimal count(int oid) {
        return myCommodityCountMapper.count_purchase(oid);
    }

    public int total(int gid, int aid, int asid, int type, ReviewType review, CompleteType complete, String search, int cid) {
        return 0;
    }

    public List<TPurchaseOrder> pagination(int gid, int aid, int asid, int type, int page, int limit, ReviewType review, CompleteType complete, String date, int cid) {
        return null;
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
