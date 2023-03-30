package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketCloudMapper;
import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.mapper.MyMarketStandardMapper;
import com.cxb.storehelperserver.repository.model.MyMarketCloud;
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
public class MarketCloudRepository extends BaseRepository<TMarketCloud> {
    @Resource
    private TMarketCloudMapper marketCloudMapper;

    @Resource
    private MarketAccountRepository marketAccountRepository;

    @Resource
    private MyMarketStandardMapper myMarketStandardMapper;

    public MarketCloudRepository() {
        init("marketCloud::");
    }

    public MyMarketCloud find(int cid) {
        TMarketCloud marketCloud = getCache(cid, TMarketCloud.class);
        if (null != marketCloud) {
            return new MyMarketCloud(marketCloud, marketAccountRepository.find(marketCloud.getAid()));
        }

        // 缓存没有就查询数据库
        TMarketCloudExample example = new TMarketCloudExample();
        example.or().andCidEqualTo(cid);
        marketCloud = marketCloudMapper.selectOneByExample(example);
        if (null != marketCloud) {
            setCache(cid, marketCloud);
            return new MyMarketCloud(marketCloud, marketAccountRepository.find(marketCloud.getAid()));
        }
        return null;
    }

    public boolean check(int aid) {
        TMarketCloudExample example = new TMarketCloudExample();
        example.or().andAidEqualTo(aid);
        List<TMarketCloud> marketClouds = marketCloudMapper.selectByExample(example);
        return null != marketClouds && !marketClouds.isEmpty();
    }

    public boolean insert(int aid, int cid) {
        TMarketCloud row = new TMarketCloud();
        row.setAid(aid);
        row.setCid(cid);
        if (marketCloudMapper.insert(row) > 0) {
            setCache(cid, row);
            return true;
        }
        return false;
    }

    public boolean update(TMarketCloud row) {
        if (marketCloudMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TMarketCloudExample example = new TMarketCloudExample();
        example.or().andCidEqualTo(cid);
        return marketCloudMapper.deleteByExample(example) > 0;
    }
}
