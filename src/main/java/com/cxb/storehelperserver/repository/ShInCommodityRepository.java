package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TShInCommodityMapper;
import com.cxb.storehelperserver.model.TShInCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 半成品入库商品仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class ShInCommodityRepository extends BaseRepository<TShInCommodity> {
    @Resource
    private TShInCommodityMapper shInCommodityMapper;

    public ShInCommodityRepository() {
        init("shInComm::");
    }

    public TShInCommodity find(int id) {
        TShInCommodity shInCommodity = getCache(id, TShInCommodity.class);
        if (null != shInCommodity) {
            return shInCommodity;
        }

        // 缓存没有就查询数据库
        shInCommodity = shInCommodityMapper.selectByPrimaryKey(id);
        if (null != shInCommodity) {
            setCache(id, shInCommodity);
        }
        return shInCommodity;
    }

    public boolean insert(TShInCommodity row) {
        if (shInCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TShInCommodity row) {
        if (shInCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return shInCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
