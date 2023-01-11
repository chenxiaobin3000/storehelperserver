package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSsInCommodityMapper;
import com.cxb.storehelperserver.model.TSsInCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 标品入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SsInCommodityRepository extends BaseRepository<TSsInCommodity> {
    @Resource
    private TSsInCommodityMapper ssInCommodityMapper;

    public SsInCommodityRepository() {
        init("ssInComm::");
    }

    public TSsInCommodity find(int id) {
        TSsInCommodity ssInCommodity = getCache(id, TSsInCommodity.class);
        if (null != ssInCommodity) {
            return ssInCommodity;
        }

        // 缓存没有就查询数据库
        ssInCommodity = ssInCommodityMapper.selectByPrimaryKey(id);
        if (null != ssInCommodity) {
            setCache(id, ssInCommodity);
        }
        return ssInCommodity;
    }

    public boolean insert(TSsInCommodity row) {
        if (ssInCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSsInCommodity row) {
        if (ssInCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return ssInCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
