package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductRemarkMapper;
import com.cxb.storehelperserver.model.TProductRemark;
import com.cxb.storehelperserver.model.TProductRemarkExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 生产备注关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class ProductRemarkRepository extends BaseRepository<List> {
    @Resource
    private TProductRemarkMapper productRemarkMapper;

    public ProductRemarkRepository() {
        init("productRemark::");
    }

    public TProductRemark find(int id) {
        return productRemarkMapper.selectByPrimaryKey(id);
    }

    public List<TProductRemark> findByOid(int oid) {
        List<TProductRemark> productRemarks = getCache(oid, List.class);
        if (null != productRemarks) {
            return productRemarks;
        }

        // 缓存没有就查询数据库
        TProductRemarkExample example = new TProductRemarkExample();
        example.or().andOidEqualTo(oid);
        productRemarks = productRemarkMapper.selectByExample(example);
        if (null != productRemarks) {
            setCache(oid, productRemarks);
        }
        return productRemarks;
    }

    public boolean insert(int oid, String remark, Date cdate) {
        TProductRemark row = new TProductRemark();
        row.setOid(oid);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (productRemarkMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TProductRemark row) {
        if (productRemarkMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TProductRemark productRemark = productRemarkMapper.selectByPrimaryKey(id);
        if (null == productRemark) {
            return false;
        }
        delCache(productRemark.getOid());
        return productRemarkMapper.deleteByPrimaryKey(id) > 0;
    }
}
