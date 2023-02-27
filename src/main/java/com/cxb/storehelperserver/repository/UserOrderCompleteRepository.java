package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserOrderCompleteMapper;
import com.cxb.storehelperserver.model.TUserOrderComplete;
import com.cxb.storehelperserver.model.TUserOrderCompleteExample;
import com.cxb.storehelperserver.repository.mapper.MyUserOrderCompleteMapper;
import com.cxb.storehelperserver.repository.model.MyUserOrderComplete;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.OrderType.*;

/**
 * desc: 用户订单完成仓库
 * auth: cxb
 * date: 2023/1/23
 */
@Slf4j
@Repository
public class UserOrderCompleteRepository extends BaseRepository<TUserOrderComplete> {
    @Resource
    private TUserOrderCompleteMapper userOrderCompleteMapper;

    @Resource
    private MyUserOrderCompleteMapper myUserOrderCompleteMapper;

    public UserOrderCompleteRepository() {
        init("userOC::");
    }

    public TUserOrderComplete find(int otype, int oid) {
        TUserOrderComplete userOrderComplete = getCache(joinKey(otype, oid), TUserOrderComplete.class);
        if (null != userOrderComplete) {
            return userOrderComplete;
        }

        // 缓存没有就查询数据库
        TUserOrderCompleteExample example = new TUserOrderCompleteExample();
        example.or().andOtypeEqualTo(otype).andOidEqualTo(oid);
        userOrderComplete = userOrderCompleteMapper.selectOneByExample(example);
        if (null != userOrderComplete) {
            setCache(joinKey(otype, oid), userOrderComplete);
        }
        return userOrderComplete;
    }

    public TUserOrderComplete findFirstOrder(int sid) {
        TUserOrderCompleteExample example = new TUserOrderCompleteExample();
        example.or().andSidEqualTo(sid);
        example.setOrderByClause("cdate asc");
        return userOrderCompleteMapper.selectOneByExample(example);
    }

    public List<MyUserOrderComplete> findByAgreement(int gid, int sid, Date start, Date end) {
        return myUserOrderCompleteMapper.selectByAgreement(gid, sid, AGREEMENT_SHIPPED_ORDER.getValue(),
                AGREEMENT_RETURN_ORDER.getValue(), new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public List<MyUserOrderComplete> findByCloud(int gid, int sid, Date start, Date end) {
        return myUserOrderCompleteMapper.selectByAgreement(gid, sid, CLOUD_PURCHASE_ORDER.getValue(),
                CLOUD_LOSS_ORDER.getValue(), new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public List<MyUserOrderComplete> findByProduct(int gid, int sid, Date start, Date end) {
        return myUserOrderCompleteMapper.selectByProduct(gid, sid, PRODUCT_PROCESS_ORDER.getValue(),
                PRODUCT_LOSS_ORDER.getValue(), new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public List<MyUserOrderComplete> findByPurchase(int gid, int sid, Date start, Date end) {
        return myUserOrderCompleteMapper.selectByProduct(gid, sid, PURCHASE_PURCHASE_ORDER.getValue(),
                PURCHASE_RETURN_ORDER.getValue(), new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public List<MyUserOrderComplete> findByStorage(int gid, int sid, Date start, Date end) {
        return myUserOrderCompleteMapper.selectByStorage(gid, sid, STORAGE_PURCHASE_ORDER.getValue(),
                STORAGE_RETURN_ORDER.getValue(), new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int total(int uid, String search) {
        TUserOrderCompleteExample example = new TUserOrderCompleteExample();
        if (null != search) {
            example.or().andUidEqualTo(uid).andBatchLike("%" + search + "%");
        } else {
            example.or().andUidEqualTo(uid);
        }
        return (int) userOrderCompleteMapper.countByExample(example);
    }

    public List<TUserOrderComplete> pagination(int uid, int page, int limit, String search) {
        TUserOrderCompleteExample example = new TUserOrderCompleteExample();
        if (null != search) {
            example.or().andUidEqualTo(uid).andBatchLike("%" + search + "%");
        } else {
            example.or().andUidEqualTo(uid);
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return userOrderCompleteMapper.selectByExample(example);
    }

    public boolean insert(TUserOrderComplete row) {
        if (userOrderCompleteMapper.insert(row) > 0) {
            setCache(joinKey(row.getOtype(), row.getOid()), row);
            return true;
        }
        return false;
    }

    public boolean update(TUserOrderComplete row) {
        if (userOrderCompleteMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getOtype(), row.getOid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int otype, int oid) {
        delCache(joinKey(otype, oid));
        TUserOrderCompleteExample example = new TUserOrderCompleteExample();
        example.or().andOtypeEqualTo(otype).andOidEqualTo(oid);
        return userOrderCompleteMapper.deleteByExample(example) > 0;
    }
}
