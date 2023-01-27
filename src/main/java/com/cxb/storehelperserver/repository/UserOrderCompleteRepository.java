package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserOrderCompleteMapper;
import com.cxb.storehelperserver.model.TUserOrderComplete;
import com.cxb.storehelperserver.model.TUserOrderCompleteExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

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
