package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityStorageMapper;
import com.cxb.storehelperserver.model.TCommodityStorage;
import com.cxb.storehelperserver.model.TCommodityStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 商品仓库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CommodityStorageRepository extends BaseRepository<List> {
    @Resource
    private TCommodityStorageMapper commodityStorageMapper;

    public CommodityStorageRepository() {
        init("commStorage::");
    }

    public List<TCommodityStorage> find(int cid) {
        List<TCommodityStorage> commodityStorages = getCache(cid, List.class);
        if (null != commodityStorages) {
            return commodityStorages;
        }

        // 缓存没有就查询数据库
        TCommodityStorageExample example = new TCommodityStorageExample();
        example.or().andCidEqualTo(cid);
        commodityStorages = commodityStorageMapper.selectByExample(example);
        if (null != commodityStorages) {
            setCache(cid, commodityStorages);
        }
        return commodityStorages;
    }

    public List<TCommodityStorage> findBySid(int sid) {
        TCommodityStorageExample example = new TCommodityStorageExample();
        example.or().andSidEqualTo(sid);
        return commodityStorageMapper.selectByExample(example);
    }

    public boolean update(int cid, List<Integer> sids) {
        delete(cid);
        TCommodityStorage row = new TCommodityStorage();
        row.setCid(cid);
        for (Integer sid : sids) {
            row.setId(0);
            row.setSid(sid);
            if (commodityStorageMapper.insert(row) <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TCommodityStorageExample example = new TCommodityStorageExample();
        example.or().andCidEqualTo(cid);
        return commodityStorageMapper.deleteByExample(example) > 0;
    }
}
