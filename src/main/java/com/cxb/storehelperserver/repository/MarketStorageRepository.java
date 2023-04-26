package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TMarketStorageMapper;
import com.cxb.storehelperserver.model.TMarketStorage;
import com.cxb.storehelperserver.model.TMarketStorageExample;
import com.cxb.storehelperserver.repository.model.MyMarketStorage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 市场账号关联仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class MarketStorageRepository extends BaseRepository<TMarketStorage> {
    @Resource
    private TMarketStorageMapper marketStorageMapper;

    @Resource
    private MarketAccountRepository marketAccountRepository;

    public MarketStorageRepository() {
        init("marketStorage::");
    }

    public MyMarketStorage find(int cid) {
        TMarketStorage marketStorage = getCache(cid, TMarketStorage.class);
        if (null != marketStorage) {
            return new MyMarketStorage(marketStorage, marketAccountRepository.find(marketStorage.getAid()));
        }

        // 缓存没有就查询数据库
        TMarketStorageExample example = new TMarketStorageExample();
        example.or().andCidEqualTo(cid);
        marketStorage = marketStorageMapper.selectOneByExample(example);
        if (null != marketStorage) {
            setCache(cid, marketStorage);
            return new MyMarketStorage(marketStorage, marketAccountRepository.find(marketStorage.getAid()));
        }
        return null;
    }

    public List<MyMarketStorage> findByAid(int aid) {
        TMarketStorageExample example = new TMarketStorageExample();
        example.or().andAidEqualTo(aid);
        val list = marketStorageMapper.selectByExample(example);
        val ret = new ArrayList<MyMarketStorage>();
        for (val marketStorage : list) {
            ret.add(new MyMarketStorage(marketStorage, marketAccountRepository.find(marketStorage.getAid())));
        }
        return ret;
    }

    public boolean check(int aid) {
        TMarketStorageExample example = new TMarketStorageExample();
        example.or().andAidEqualTo(aid);
        return null != marketStorageMapper.selectOneByExample(example);
    }

    public boolean insert(int aid, int cid) {
        TMarketStorage row = new TMarketStorage();
        row.setAid(aid);
        row.setCid(cid);
        if (marketStorageMapper.insert(row) > 0) {
            setCache(cid, row);
            return true;
        }
        return false;
    }

    public boolean update(TMarketStorage row) {
        if (marketStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TMarketStorageExample example = new TMarketStorageExample();
        example.or().andCidEqualTo(cid);
        return marketStorageMapper.deleteByExample(example) > 0;
    }
}
