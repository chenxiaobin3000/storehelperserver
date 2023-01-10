package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TScOutCommodityMapper;
import com.cxb.storehelperserver.model.TScOutCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 商品出库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class ScOutCommodityRepository extends BaseRepository<TScOutCommodity> {
    @Resource
    private TScOutCommodityMapper scOutCommodityMapper;

    public ScOutCommodityRepository() {
        init("scOutComm::");
    }

    public TScOutCommodity find(int id) {
        TScOutCommodity scOutCommodity = getCache(id, TScOutCommodity.class);
        if (null != scOutCommodity) {
            return scOutCommodity;
        }

        // 缓存没有就查询数据库
        scOutCommodity = scOutCommodityMapper.selectByPrimaryKey(id);
        if (null != scOutCommodity) {
            setCache(id, scOutCommodity);
        }
        return scOutCommodity;
    }

    public boolean insert(TScOutCommodity row) {
        if (scOutCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TScOutCommodity row) {
        if (scOutCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return scOutCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
