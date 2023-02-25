package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseFareMapper;
import com.cxb.storehelperserver.model.TPurchaseFare;
import com.cxb.storehelperserver.model.TPurchaseFareExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * desc: 采购物流费用仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class PurchaseFareRepository extends BaseRepository<List> {
    @Resource
    private TPurchaseFareMapper purchaseFareMapper;

    public PurchaseFareRepository() {
        init("purFare::");
    }

    public List<TPurchaseFare> find(int oid) {
        List<TPurchaseFare> purchaseFares = getCache(oid, List.class);
        if (null != purchaseFares) {
            return purchaseFares;
        }

        // 缓存没有就查询数据库
        TPurchaseFareExample example = new TPurchaseFareExample();
        example.or().andOidEqualTo(oid);
        purchaseFares = purchaseFareMapper.selectByExample(example);
        if (null != purchaseFares) {
            setCache(oid, purchaseFares);
        }
        return purchaseFares;
    }

    public boolean insert(int oid, BigDecimal fare) {
        TPurchaseFare row = new TPurchaseFare();
        row.setOid(oid);
        row.setFare(fare);
        if (purchaseFareMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TPurchaseFare row) {
        if (purchaseFareMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TPurchaseFare purchaseFare = purchaseFareMapper.selectByPrimaryKey(id);
        if (null == purchaseFare) {
            return false;
        }
        delCache(purchaseFare.getOid());
        return purchaseFareMapper.deleteByPrimaryKey(id) > 0;
    }
}
