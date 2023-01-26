package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserOrderApplyMapper;
import com.cxb.storehelperserver.model.TUserOrderApply;
import com.cxb.storehelperserver.model.TUserOrderApplyExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 用户订单申请仓库
 * auth: cxb
 * date: 2023/1/23
 */
@Slf4j
@Repository
public class UserOrderApplyRepository extends BaseRepository<TUserOrderApply> {
    @Resource
    private TUserOrderApplyMapper userOrderApplyMapper;

    public UserOrderApplyRepository() {
        init("userOA::");
    }

    public TUserOrderApply find(int otype, int oid) {
        TUserOrderApply userOrderApply = getCache(joinKey(otype, oid), TUserOrderApply.class);
        if (null != userOrderApply) {
            return userOrderApply;
        }

        // 缓存没有就查询数据库
        TUserOrderApplyExample example = new TUserOrderApplyExample();
        example.or().andOtypeEqualTo(otype).andOidEqualTo(oid);
        userOrderApply = userOrderApplyMapper.selectOneByExample(example);
        if (null != userOrderApply) {
            setCache(joinKey(otype, oid), userOrderApply);
        }
        return userOrderApply;
    }

    public int total(int uid, String search) {
        TUserOrderApplyExample example = new TUserOrderApplyExample();
        if (null != search) {
            example.or().andUidEqualTo(uid).andBatchLike("%" + search + "%");
        } else {
            example.or().andUidEqualTo(uid);
        }
        return (int) userOrderApplyMapper.countByExample(example);
    }

    public List<TUserOrderApply> pagination(int uid, int page, int limit, String search) {
        TUserOrderApplyExample example = new TUserOrderApplyExample();
        if (null != search) {
            example.or().andUidEqualTo(uid).andBatchLike("%" + search + "%");
        } else {
            example.or().andUidEqualTo(uid);
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return userOrderApplyMapper.selectByExample(example);
    }

    public boolean insert(TUserOrderApply row) {
        if (userOrderApplyMapper.insert(row) > 0) {
            setCache(joinKey(row.getOtype(), row.getOid()), row);
            return true;
        }
        return false;
    }

    public boolean update(TUserOrderApply row) {
        if (userOrderApplyMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getOtype(), row.getOid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int otype, int oid) {
        delCache(joinKey(otype, oid));
        TUserOrderApplyExample example = new TUserOrderApplyExample();
        example.or().andOtypeEqualTo(otype).andOidEqualTo(oid);
        return userOrderApplyMapper.deleteByExample(example) > 0;
    }
}
