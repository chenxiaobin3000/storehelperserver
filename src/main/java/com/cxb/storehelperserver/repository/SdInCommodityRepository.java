package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSdInCommodityMapper;
import com.cxb.storehelperserver.model.TSdInCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 废料入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SdInCommodityRepository extends BaseRepository<TSdInCommodity> {
    @Resource
    private TSdInCommodityMapper sdInCommodityMapper;

    public SdInCommodityRepository() {
        init("sdInComm::");
    }

    public TSdInCommodity find(int id) {
        TSdInCommodity sdInCommodity = getCache(id, TSdInCommodity.class);
        if (null != sdInCommodity) {
            return sdInCommodity;
        }

        // 缓存没有就查询数据库
        sdInCommodity = sdInCommodityMapper.selectByPrimaryKey(id);
        if (null != sdInCommodity) {
            setCache(id, sdInCommodity);
        }
        return sdInCommodity;
    }

    public boolean insert(TSdInCommodity row) {
        if (sdInCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSdInCommodity row) {
        if (sdInCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return sdInCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
