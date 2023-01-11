package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSdOutCommodityMapper;
import com.cxb.storehelperserver.model.TSdOutCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 废料出库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SdOutCommodityRepository extends BaseRepository<TSdOutCommodity> {
    @Resource
    private TSdOutCommodityMapper sdOutCommodityMapper;

    public SdOutCommodityRepository() {
        init("sdOutComm::");
    }

    public TSdOutCommodity find(int id) {
        TSdOutCommodity sdOutCommodity = getCache(id, TSdOutCommodity.class);
        if (null != sdOutCommodity) {
            return sdOutCommodity;
        }

        // 缓存没有就查询数据库
        sdOutCommodity = sdOutCommodityMapper.selectByPrimaryKey(id);
        if (null != sdOutCommodity) {
            setCache(id, sdOutCommodity);
        }
        return sdOutCommodity;
    }

    public boolean insert(TSdOutCommodity row) {
        if (sdOutCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSdOutCommodity row) {
        if (sdOutCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return sdOutCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
