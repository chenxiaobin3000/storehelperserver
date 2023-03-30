package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketManyMapper;
import com.cxb.storehelperserver.model.TMarketMany;
import com.cxb.storehelperserver.model.TMarketManyExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 市场聚合账号仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketManyRepository extends BaseRepository<TMarketMany> {
    @Resource
    private TMarketManyMapper marketManyMapper;

    public MarketManyRepository() {
        init("marketMany::");
    }

    public TMarketMany find(int id) {
        TMarketMany marketMany = getCache(id, TMarketMany.class);
        if (null != marketMany) {
            return marketMany;
        }

        // 缓存没有就查询数据库
        marketMany = marketManyMapper.selectByPrimaryKey(id);
        if (null != marketMany) {
            setCache(id, marketMany);
        }
        return marketMany;
    }

    public List<TMarketMany> findByAid(int aid) {
        TMarketManyExample example = new TMarketManyExample();
        example.or().andAidEqualTo(aid);
        return marketManyMapper.selectByExample(example);
    }

    public int total(int gid) {
        // 包含搜索的不缓存
        int total = getTotalCache(gid);
        if (0 != total) {
            return total;
        }
        TMarketManyExample example = new TMarketManyExample();
        example.or().andGidEqualTo(gid);
        example.setGroupBy("aid");
        total = (int) marketManyMapper.countByExample(example);
        setTotalCache(gid, total);
        return total;
    }

    public List<TMarketMany> pagination(int gid, int page, int limit) {
        TMarketManyExample example = new TMarketManyExample();
        example.or().andGidEqualTo(gid);
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return marketManyMapper.selectByExample(example);
    }

    public boolean insert(int gid, int mid, int aid, String account, String remark) {
        TMarketMany row = new TMarketMany();
        row.setGid(gid);
        row.setMid(mid);
        row.setAid(aid);
        row.setAccount(account);
        row.setRemark(remark);
        if (marketManyMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(gid);
            return true;
        }
        return false;
    }

    public boolean update(TMarketMany row) {
        if (marketManyMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TMarketMany account = find(id);
        if (null == account) {
            return false;
        }
        delCache(id);
        delTotalCache(account.getGid());
        return marketManyMapper.deleteByPrimaryKey(id) > 0;
    }
}
