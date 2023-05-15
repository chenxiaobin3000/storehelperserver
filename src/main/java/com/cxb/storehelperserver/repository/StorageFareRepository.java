package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageFareMapper;
import com.cxb.storehelperserver.model.TProductFare;
import com.cxb.storehelperserver.model.TStorageFare;
import com.cxb.storehelperserver.model.TStorageFareExample;
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
 * desc: 仓储物流费用仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StorageFareRepository extends BaseRepository<List> {
    @Resource
    private TStorageFareMapper storageFareMapper;

    @Resource
    private MyFareMapper myFareMapper;

    public StorageFareRepository() {
        init("storageFare::");
    }

    public TStorageFare find(int id) {
        return storageFareMapper.selectByPrimaryKey(id);
    }

    public List<TStorageFare> findByOid(int oid) {
        List<TStorageFare> storageFares = getCache(oid, List.class);
        if (null != storageFares) {
            return storageFares;
        }

        // 缓存没有就查询数据库
        TStorageFareExample example = new TStorageFareExample();
        example.or().andOidEqualTo(oid);
        storageFares = storageFareMapper.selectByExample(example);
        if (null != storageFares) {
            setCache(oid, storageFares);
        }
        return storageFares;
    }

    public int total(int gid, int sid, int type, ReviewType review, CompleteType complete, Date start, Date end) {
        return myFareMapper.countStorageOrder(gid, sid, type, review.getValue(), complete.getValue(), start, end);
    }

    public List<TStorageFare> pagination(int gid, int sid, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end) {
        return myFareMapper.paginationStorageOrder((page - 1) * limit, limit, gid, sid, type, review.getValue(), complete.getValue(), start, end);
    }

    public boolean insert(int oid, String ship, String code, String phone, BigDecimal fare, String remark, Date cdate) {
        TStorageFare row = new TStorageFare();
        row.setOid(oid);
        row.setShip(ship);
        row.setCode(code);
        row.setPhone(phone);
        row.setFare(fare);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (storageFareMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TStorageFare row) {
        if (storageFareMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageFare storageFare = storageFareMapper.selectByPrimaryKey(id);
        if (null == storageFare) {
            return false;
        }
        delCache(storageFare.getOid());
        return storageFareMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int oid) {
        delCache(oid);
        return myFareMapper.setStorageFareReviewNull(oid) > 0;
    }
}
