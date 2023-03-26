package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityCloudMapper;
import com.cxb.storehelperserver.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 商品云仓关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CommodityCloudRepository extends BaseRepository<List> {
    @Resource
    private TCommodityCloudMapper commodityCloudMapper;

    public CommodityCloudRepository() {
        init("commCloud::");
    }

    public List<TCommodityCloud> find(int cid) {
        List<TCommodityCloud> commodityClouds = getCache(cid, List.class);
        if (null != commodityClouds) {
            return commodityClouds;
        }

        // 缓存没有就查询数据库
        TCommodityCloudExample example = new TCommodityCloudExample();
        example.or().andCidEqualTo(cid);
        commodityClouds = commodityCloudMapper.selectByExample(example);
        if (null != commodityClouds) {
            setCache(cid, commodityClouds);
        }
        return commodityClouds;
    }

    public boolean update(int cid, List<Integer> sids) {
        delete(cid);
        TCommodityCloud row = new TCommodityCloud();
        row.setCid(cid);
        for (Integer sid : sids) {
            row.setId(0);
            row.setSid(sid);
            if (commodityCloudMapper.insert(row) <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TCommodityCloudExample example = new TCommodityCloudExample();
        example.or().andCidEqualTo(cid);
        return commodityCloudMapper.deleteByExample(example) > 0;
    }
}
