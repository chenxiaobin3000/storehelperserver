package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseRemarkMapper;
import com.cxb.storehelperserver.model.TPurchaseRemark;
import com.cxb.storehelperserver.model.TPurchaseRemarkExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 采购退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class PurchaseRemarkRepository extends BaseRepository<TPurchaseRemark> {
    @Resource
    private TPurchaseRemarkMapper purchaseRemarkMapper;

    public PurchaseRemarkRepository() {
        init("purRemark::");
    }

    public TPurchaseRemark find(int oid) {
        TPurchaseRemark purchaseRemark = getCache(oid, TPurchaseRemark.class);
        if (null != purchaseRemark) {
            return purchaseRemark;
        }

        // 缓存没有就查询数据库
        TPurchaseRemarkExample example = new TPurchaseRemarkExample();
        example.or().andOidEqualTo(oid);
        purchaseRemark = purchaseRemarkMapper.selectOneByExample(example);
        if (null != purchaseRemark) {
            setCache(oid, purchaseRemark);
        }
        return purchaseRemark;
    }

    public boolean insert(int oid, String remark) {
        TPurchaseRemark row = new TPurchaseRemark();
        row.setOid(oid);
        row.setRemark(remark);
        if (purchaseRemarkMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TPurchaseRemark row) {
        if (purchaseRemarkMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TPurchaseRemark purchaseRemark = purchaseRemarkMapper.selectByPrimaryKey(id);
        if (null == purchaseRemark) {
            return false;
        }
        delCache(purchaseRemark.getOid());
        return purchaseRemarkMapper.deleteByPrimaryKey(id) > 0;
    }
}
