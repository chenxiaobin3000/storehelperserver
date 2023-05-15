package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductFareMapper;
import com.cxb.storehelperserver.model.TProductFare;
import com.cxb.storehelperserver.model.TProductFareExample;
import com.cxb.storehelperserver.repository.mapper.MyFareMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.cxb.storehelperserver.util.TypeDefine.ReviewType;
import com.cxb.storehelperserver.util.TypeDefine.CompleteType;

/**
 * desc: 生产物流费用仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class ProductFareRepository extends BaseRepository<List> {
    @Resource
    private TProductFareMapper productFareMapper;

    @Resource
    private MyFareMapper myFareMapper;

    public ProductFareRepository() {
        init("productFare::");
    }

    public TProductFare find(int id) {
        return productFareMapper.selectByPrimaryKey(id);
    }

    public List<TProductFare> findByOid(int oid) {
        List<TProductFare> productFares = getCache(oid, List.class);
        if (null != productFares) {
            return productFares;
        }

        // 缓存没有就查询数据库
        TProductFareExample example = new TProductFareExample();
        example.or().andOidEqualTo(oid);
        productFares = productFareMapper.selectByExample(example);
        if (null != productFares) {
            setCache(oid, productFares);
        }
        return productFares;
    }

    public int total(int gid, int sid, int type, ReviewType review, CompleteType complete, Date start, Date end) {
        return myFareMapper.countProductOrder(gid, sid, type, review.getValue(), complete.getValue(), start, end);
    }

    public List<TProductFare> pagination(int gid, int sid, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end) {
        return myFareMapper.paginationProductOrder((page - 1) * limit, limit, gid, sid, type, review.getValue(), complete.getValue(), start, end);
    }

    public boolean insert(int oid, String ship, String code, String phone, BigDecimal fare, String remark, Date cdate) {
        TProductFare row = new TProductFare();
        row.setOid(oid);
        row.setShip(ship);
        row.setCode(code);
        row.setPhone(phone);
        row.setFare(fare);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (productFareMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TProductFare row) {
        if (productFareMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TProductFare productFare = productFareMapper.selectByPrimaryKey(id);
        if (null == productFare) {
            return false;
        }
        delCache(productFare.getOid());
        return productFareMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int oid) {
        delCache(oid);
        return myFareMapper.setProductFareReviewNull(oid) > 0;
    }
}
