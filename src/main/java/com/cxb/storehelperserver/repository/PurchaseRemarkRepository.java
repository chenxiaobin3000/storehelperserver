package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseRemarkMapper;
import com.cxb.storehelperserver.model.TPurchaseRemark;
import com.cxb.storehelperserver.model.TPurchaseRemarkExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 采购备注关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class PurchaseRemarkRepository extends BaseRepository<List> {
    @Resource
    private TPurchaseRemarkMapper purchaseRemarkMapper;

    public PurchaseRemarkRepository() {
        init("purRemark::");
    }

    public TPurchaseRemark find(int id) {
        return purchaseRemarkMapper.selectByPrimaryKey(id);
    }

    public List<TPurchaseRemark> findByOid(int oid) {
        List<TPurchaseRemark> purchaseRemarks = getCache(oid, List.class);
        if (null != purchaseRemarks) {
            return purchaseRemarks;
        }

        // 缓存没有就查询数据库
        TPurchaseRemarkExample example = new TPurchaseRemarkExample();
        example.or().andOidEqualTo(oid);
        purchaseRemarks = purchaseRemarkMapper.selectByExample(example);
        if (null != purchaseRemarks) {
            setCache(oid, purchaseRemarks);
        }
        return purchaseRemarks;
    }

    public boolean insert(int oid, String remark, Date cdate) {
        TPurchaseRemark row = new TPurchaseRemark();
        row.setOid(oid);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (purchaseRemarkMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TPurchaseRemark row) {
        if (purchaseRemarkMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
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
