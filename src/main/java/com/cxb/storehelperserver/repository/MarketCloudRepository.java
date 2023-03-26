package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketAccountMapper;
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
public class MarketCloudRepository extends BaseRepository<MyMarketCloud> {
    @Resource
    private TMarketCloudMapper marketCloudMapper;

    @Resource
    private TMarketAccountMapper marketAccountMapper;

    @Resource
    private MyMarketStandardMapper myMarketStandardMapper;

    public MarketCloudRepository() {
        init("marketCloud::");
    }

    public MyMarketCloud find(int cid) {
        MyMarketCloud marketCloud = getCache(cid, MyMarketCloud.class);
        if (null != marketCloud) {
            return marketCloud;
        }

        // 缓存没有就查询数据库
        TMarketCloudExample example = new TMarketCloudExample();
        example.or().andCidEqualTo(cid);
        TMarketCloud cloud = marketCloudMapper.selectOneByExample(example);
        if (null != cloud) {
            marketCloud = create(cloud, cid);
        }
        return marketCloud;
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
            create(row, cid);
            return true;
        }
        return false;
    }

    public boolean update(TMarketCloud row) {
        if (marketCloudMapper.updateByPrimaryKey(row) > 0) {
            create(row, row.getCid());
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

    private MyMarketCloud create(TMarketCloud cloud, int cid) {
        MyMarketCloud marketCloud = new MyMarketCloud();
        marketCloud.setId(cloud.getId());
        marketCloud.setCid(cid);
        TMarketAccount account = marketAccountMapper.selectByPrimaryKey(cloud.getAid());
        if (null != account) {
            marketCloud.setGid(account.getGid());
            marketCloud.setMid(account.getMid());
            marketCloud.setAccount(account.getAccount());
        }
        setCache(cid, marketCloud);
        return marketCloud;
    }
}
