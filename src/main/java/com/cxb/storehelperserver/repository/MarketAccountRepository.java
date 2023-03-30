package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketAccountMapper;
import com.cxb.storehelperserver.model.TMarketAccount;
import com.cxb.storehelperserver.model.TMarketAccountExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 市场账号关联仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketAccountRepository extends BaseRepository<TMarketAccount> {
    @Resource
    private TMarketAccountMapper marketAccountMapper;

    public MarketAccountRepository() {
        init("marketAccount::");
    }

    public TMarketAccount find(int id) {
        TMarketAccount account = getCache(id, TMarketAccount.class);
        if (null != account) {
            return account;
        }
        account = marketAccountMapper.selectByPrimaryKey(id);
        if (null != account) {
            setCache(id, account);
        }
        return account;
    }

    public List<TMarketAccount> find(int gid, int mid) {
        TMarketAccountExample example = new TMarketAccountExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        return marketAccountMapper.selectByExample(example);
    }

    public int total(int gid, int mid) {
        // 包含搜索的不缓存
        String key = joinKey(gid, mid);
        int total = getTotalCache(key);
        if (0 != total) {
            return total;
        }
        TMarketAccountExample example = new TMarketAccountExample();
        if (0 == mid) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        }
        total = (int) marketAccountMapper.countByExample(example);
        setTotalCache(key, total);
        return total;
    }

    public List<TMarketAccount> pagination(int gid, int mid, int page, int limit) {
        TMarketAccountExample example = new TMarketAccountExample();
        if (0 == mid) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return marketAccountMapper.selectByExample(example);
    }

    public boolean insert(int gid, int mid, String account, String remark) {
        TMarketAccount row = new TMarketAccount();
        row.setGid(gid);
        row.setMid(mid);
        row.setAccount(account);
        row.setRemark(remark);
        if (marketAccountMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(joinKey(gid, mid));
            return true;
        }
        return false;
    }

    public boolean update(TMarketAccount row) {
        if (marketAccountMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(joinKey(row.getGid(), row.getMid()));
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TMarketAccount account = find(id);
        if (null == account) {
            return false;
        }
        delCache(id);
        delTotalCache(joinKey(account.getGid(), account.getMid()));
        return marketAccountMapper.deleteByPrimaryKey(id) > 0;
    }
}
