package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TGroupMarketMapper;
import com.cxb.storehelperserver.model.TGroupMarket;
import com.cxb.storehelperserver.model.TGroupMarketExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 公司关联的销售平台仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class GroupMarketRepository extends BaseRepository<List> {
    @Resource
    private TGroupMarketMapper groupMarketMapper;

    public GroupMarketRepository() {
        init("groupMarket::");
    }

    public List<TGroupMarket> find(int gid) {
        List<TGroupMarket> groupMarkets = getCache(gid, List.class);
        if (null != groupMarkets) {
            return groupMarkets;
        }

        // 缓存没有就查询数据库
        TGroupMarketExample example = new TGroupMarketExample();
        example.or().andGidEqualTo(gid);
        groupMarkets = groupMarketMapper.selectByExample(example);
        if (null != groupMarkets) {
            setCache(gid, groupMarkets);
        }
        return groupMarkets;
    }

    /*
     * desc: 判断公司是否存在平台权限
     */
    public boolean check(int gid, int mid) {
        List<TGroupMarket> groupMarkets = getCache(gid, List.class);
        if (null != groupMarkets && !groupMarkets.isEmpty()) {
            for (TGroupMarket market : groupMarkets) {
                if (market.getMid().equals(mid)) {
                    return true;
                }
            }
            return false;
        }
        TGroupMarketExample example = new TGroupMarketExample();
        example.or().andGidEqualTo(gid).andMidEqualTo(mid);
        return null != groupMarketMapper.selectOneByExample(example);
    }

    public boolean insert(TGroupMarket row) {
        if (groupMarketMapper.insert(row) > 0) {
            delCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TGroupMarket row) {
        if (groupMarketMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int gid) {
        delCache(gid);
        TGroupMarketExample example = new TGroupMarketExample();
        example.or().andGidEqualTo(gid);
        return groupMarketMapper.deleteByExample(example) > 0;
    }
}
