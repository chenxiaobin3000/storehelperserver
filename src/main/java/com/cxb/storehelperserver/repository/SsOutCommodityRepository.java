package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSsOutCommodityMapper;
import com.cxb.storehelperserver.model.TSsOutCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 标品出库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SsOutCommodityRepository extends BaseRepository<TSsOutCommodity> {
    @Resource
    private TSsOutCommodityMapper sdOutCommodityMapper;

    public SsOutCommodityRepository() {
        init("sdOutComm::");
    }

    public TSsOutCommodity find(int id) {
        TSsOutCommodity sdOutCommodity = getCache(id, TSsOutCommodity.class);
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

    public boolean insert(TSsOutCommodity row) {
        if (sdOutCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSsOutCommodity row) {
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
