package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityCloudMapper;
import com.cxb.storehelperserver.model.TCommodityCloud;
import com.cxb.storehelperserver.model.TCommodityCloudExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 商品云仓关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CommodityCloudRepository extends BaseRepository<TCommodityCloud> {
    @Resource
    private TCommodityCloudMapper commodityCloudMapper;

    public CommodityCloudRepository() {
        init("commCloud::");
    }

    public TCommodityCloud find(int cid) {
        TCommodityCloud commodityCloud = getCache(cid, TCommodityCloud.class);
        if (null != commodityCloud) {
            return commodityCloud;
        }

        // 缓存没有就查询数据库
        TCommodityCloudExample example = new TCommodityCloudExample();
        example.or().andCidEqualTo(cid);
        commodityCloud = commodityCloudMapper.selectOneByExample(example);
        if (null != commodityCloud) {
            setCache(cid, commodityCloud);
        }
        return commodityCloud;
    }

    public boolean insert(TCommodityCloud row) {
        if (commodityCloudMapper.insert(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TCommodityCloud row) {
        if (commodityCloudMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TCommodityCloudExample example = new TCommodityCloudExample();
        example.or().andCidEqualTo(cid);
        return commodityCloudMapper.deleteByExample(example) > 0;
    }
}
