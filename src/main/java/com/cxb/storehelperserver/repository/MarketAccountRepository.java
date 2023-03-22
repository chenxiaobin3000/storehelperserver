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
public class MarketAccountRepository extends BaseRepository<List> {
    @Resource
    private TMarketAccountMapper marketAccountMapper;

    public MarketAccountRepository() {
        init("marketAccount::");
    }

    public List<TMarketAccount> find(int gid, int mid) {
        String key = joinKey(gid, mid);
        List<TMarketAccount> marketAccounts = getCache(key, List.class);
        if (null != marketAccounts) {
            return marketAccounts;
        }

        // 缓存没有就查询数据库
        TMarketAccountExample example = new TMarketAccountExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        marketAccounts = marketAccountMapper.selectByExample(example);
        if (null != marketAccounts) {
            setCache(key, marketAccounts);
        }
        return marketAccounts;
    }

    public int total(int gid, int mid) {
        // 包含搜索的不缓存
        String key = joinKey(gid, mid);
        int total = getTotalCache(key);
        if (0 != total) {
            return total;
        }
        TMarketAccountExample example = new TMarketAccountExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        total = (int) marketAccountMapper.countByExample(example);
        setTotalCache(key, total);
        return total;
    }

    public List<TMarketAccount> pagination(int gid, int mid, int page, int limit) {
        TMarketAccountExample example = new TMarketAccountExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return marketAccountMapper.selectByExample(example);
    }

    public boolean insert(int gid, int mid, String account) {
        TMarketAccount row = new TMarketAccount();
        row.setGid(gid);
        row.setMid(mid);
        row.setAccount(account);
        if (marketAccountMapper.insert(row) > 0) {
            String key = joinKey(gid, mid);
            delCache(key);
            delTotalCache(key);
            return true;
        }
        return false;
    }

    public boolean update(TMarketAccount row) {
        if (marketAccountMapper.updateByPrimaryKey(row) > 0) {
            String key = joinKey(row.getGid(), row.getMid());
            delCache(key);
            delTotalCache(key);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TMarketAccount account = marketAccountMapper.selectByPrimaryKey(id);
        if (null == account) {
            return false;
        }
        String key = joinKey(account.getGid(), account.getMid());
        delCache(key);
        delTotalCache(key);
        return marketAccountMapper.deleteByPrimaryKey(id) > 0;
    }
}
