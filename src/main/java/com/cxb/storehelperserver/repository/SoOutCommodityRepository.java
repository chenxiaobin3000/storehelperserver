package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSoOutCommodityMapper;
import com.cxb.storehelperserver.model.TSoOutCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料出库原料仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SoOutCommodityRepository extends BaseRepository<TSoOutCommodity> {
    @Resource
    private TSoOutCommodityMapper soOutCommodityMapper;

    public SoOutCommodityRepository() {
        init("soOutComm::");
    }

    public TSoOutCommodity find(int id) {
        TSoOutCommodity soOutCommodity = getCache(id, TSoOutCommodity.class);
        if (null != soOutCommodity) {
            return soOutCommodity;
        }

        // 缓存没有就查询数据库
        soOutCommodity = soOutCommodityMapper.selectByPrimaryKey(id);
        if (null != soOutCommodity) {
            setCache(id, soOutCommodity);
        }
        return soOutCommodity;
    }

    public boolean insert(TSoOutCommodity row) {
        if (soOutCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSoOutCommodity row) {
        if (soOutCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return soOutCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
