package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TScInCommodityMapper;
import com.cxb.storehelperserver.model.TScInCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 商品入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class ScInCommodityRepository extends BaseRepository<TScInCommodity> {
    @Resource
    private TScInCommodityMapper scInCommodityMapper;

    public ScInCommodityRepository() {
        init("scInComm::");
    }

    public TScInCommodity find(int id) {
        TScInCommodity scInCommodity = getCache(id, TScInCommodity.class);
        if (null != scInCommodity) {
            return scInCommodity;
        }

        // 缓存没有就查询数据库
        scInCommodity = scInCommodityMapper.selectByPrimaryKey(id);
        if (null != scInCommodity) {
            setCache(id, scInCommodity);
        }
        return scInCommodity;
    }

    public boolean insert(TScInCommodity row) {
        if (scInCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TScInCommodity row) {
        if (scInCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return scInCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
