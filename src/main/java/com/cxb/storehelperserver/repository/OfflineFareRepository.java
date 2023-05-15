package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOfflineFareMapper;
import com.cxb.storehelperserver.model.TOfflineFare;
import com.cxb.storehelperserver.model.TOfflineFareExample;
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
 * desc: 线下销售物流费用仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OfflineFareRepository extends BaseRepository<List> {
    @Resource
    private TOfflineFareMapper offlineFareMapper;

    @Resource
    private MyFareMapper myFareMapper;

    public OfflineFareRepository() {
        init("offlineFare::");
    }

    public TOfflineFare find(int id) {
        return offlineFareMapper.selectByPrimaryKey(id);
    }

    public List<TOfflineFare> findByOid(int oid) {
        List<TOfflineFare> offlineFares = getCache(oid, List.class);
        if (null != offlineFares) {
            return offlineFares;
        }

        // 缓存没有就查询数据库
        TOfflineFareExample example = new TOfflineFareExample();
        example.or().andOidEqualTo(oid);
        offlineFares = offlineFareMapper.selectByExample(example);
        if (null != offlineFares) {
            setCache(oid, offlineFares);
        }
        return offlineFares;
    }

    public int total(int gid, int aid, int sid, int type, ReviewType review, CompleteType complete, Date start, Date end) {
        return myFareMapper.countOfflineOrder(gid, aid, sid, type, review.getValue(), complete.getValue(), start, end);
    }

    public List<TOfflineFare> pagination(int gid, int aid, int sid, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end) {
        return myFareMapper.paginationOfflineOrder((page - 1) * limit, limit, gid, aid, sid, type, review.getValue(), complete.getValue(), start, end);
    }

    public boolean insert(int oid, String ship, String code, String phone, BigDecimal fare, String remark, Date cdate) {
        TOfflineFare row = new TOfflineFare();
        row.setOid(oid);
        row.setShip(ship);
        row.setCode(code);
        row.setPhone(phone);
        row.setFare(fare);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (offlineFareMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TOfflineFare row) {
        if (offlineFareMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TOfflineFare offlineFare = offlineFareMapper.selectByPrimaryKey(id);
        if (null == offlineFare) {
            return false;
        }
        delCache(offlineFare.getOid());
        return offlineFareMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int oid) {
        delCache(oid);
        return myFareMapper.setOfflineFareReviewNull(oid) > 0;
    }
}
