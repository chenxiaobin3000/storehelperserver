package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TShOutCommodityMapper;
import com.cxb.storehelperserver.model.TShOutCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 半成品出库商品仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class ShOutCommodityRepository extends BaseRepository<TShOutCommodity> {
    @Resource
    private TShOutCommodityMapper shOutCommodityMapper;

    public ShOutCommodityRepository() {
        init("shOutComm::");
    }

    public TShOutCommodity find(int id) {
        TShOutCommodity shOutCommodity = getCache(id, TShOutCommodity.class);
        if (null != shOutCommodity) {
            return shOutCommodity;
        }

        // 缓存没有就查询数据库
        shOutCommodity = shOutCommodityMapper.selectByPrimaryKey(id);
        if (null != shOutCommodity) {
            setCache(id, shOutCommodity);
        }
        return shOutCommodity;
    }

    public boolean insert(TShOutCommodity row) {
        if (shOutCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TShOutCommodity row) {
        if (shOutCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return shOutCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
