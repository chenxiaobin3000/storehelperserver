package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TFinanceDetailMapper;
import com.cxb.storehelperserver.model.TFinanceDetail;
import com.cxb.storehelperserver.model.TFinanceDetailExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 类目资金明细仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class FinanceDetailRepository extends BaseRepository<TFinanceDetail> {
    @Resource
    private TFinanceDetailMapper financeDetailMapper;

    public FinanceDetailRepository() {
        init("financeDetail::");
    }

    public TFinanceDetail find(int id) {
        TFinanceDetail financeDetail = getCache(id, TFinanceDetail.class);
        if (null != financeDetail) {
            return financeDetail;
        }

        // 缓存没有就查询数据库
        financeDetail = financeDetailMapper.selectByPrimaryKey(id);
        if (null != financeDetail) {
            setCache(id, financeDetail);
        }
        return financeDetail;
    }

    public int total(int gid, int action, Date date) {
        int total = getTotalCache(gid);
        if (0 != total) {
            return total;
        }
        TFinanceDetailExample example = new TFinanceDetailExample();
        if (0 == action) {
            example.or().andGidEqualTo(gid).andCdateEqualTo(date);
        } else {
            example.or().andGidEqualTo(gid).andCdateEqualTo(date).andActionEqualTo(action);
        }
        total = (int) financeDetailMapper.countByExample(example);
        setTotalCache(gid, total);
        return total;
    }

    public List<TFinanceDetail> pagination(int gid, int page, int limit, int action, Date date) {
        TFinanceDetailExample example = new TFinanceDetailExample();
        if (0 == action) {
            example.or().andGidEqualTo(gid).andCdateEqualTo(date);
        } else {
            example.or().andGidEqualTo(gid).andCdateEqualTo(date).andActionEqualTo(action);
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return financeDetailMapper.selectByExample(example);
    }

    public boolean insert(TFinanceDetail row) {
        if (financeDetailMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TFinanceDetail row) {
        if (financeDetailMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TFinanceDetail financeDetail = find(id);
        if (null == financeDetail) {
            return false;
        }
        delCache(id);
        delTotalCache(financeDetail.getGid());
        return financeDetailMapper.deleteByPrimaryKey(id) > 0;
    }
}
