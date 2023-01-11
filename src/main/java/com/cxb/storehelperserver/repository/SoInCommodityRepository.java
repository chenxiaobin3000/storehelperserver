package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSoInCommodityMapper;
import com.cxb.storehelperserver.model.TSoInCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料入库原料仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SoInCommodityRepository extends BaseRepository<TSoInCommodity> {
    @Resource
    private TSoInCommodityMapper soInCommodityMapper;

    public SoInCommodityRepository() {
        init("soInComm::");
    }

    public TSoInCommodity find(int id) {
        TSoInCommodity soInCommodity = getCache(id, TSoInCommodity.class);
        if (null != soInCommodity) {
            return soInCommodity;
        }

        // 缓存没有就查询数据库
        soInCommodity = soInCommodityMapper.selectByPrimaryKey(id);
        if (null != soInCommodity) {
            setCache(id, soInCommodity);
        }
        return soInCommodity;
    }

    public boolean insert(TSoInCommodity row) {
        if (soInCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSoInCommodity row) {
        if (soInCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return soInCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
