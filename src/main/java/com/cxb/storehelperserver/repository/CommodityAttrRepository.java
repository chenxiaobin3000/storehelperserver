package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityAttrMapper;
import com.cxb.storehelperserver.model.TCommodityAttr;
import com.cxb.storehelperserver.model.TCommodityAttrExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 商品属性仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class CommodityAttrRepository extends BaseRepository<TCommodityAttr> {
    @Resource
    private TCommodityAttrMapper commodityAttrMapper;

    private final String cacheCommName;

    public CommodityAttrRepository() {
        init("commAttr::");
        cacheCommName = cacheName + "comm::";
    }

    public TCommodityAttr find(int id) {
        TCommodityAttr commodityAttr = getCache(id, TCommodityAttr.class);
        if (null != commodityAttr) {
            return commodityAttr;
        }

        // 缓存没有就查询数据库
        commodityAttr = commodityAttrMapper.selectByPrimaryKey(id);
        if (null != commodityAttr) {
            setCache(id, commodityAttr);
        }
        return commodityAttr;
    }

    public List<TCommodityAttr> findByCommodity(int cid) {
        List<TCommodityAttr> commoditieAtrrs = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheCommName + cid));
        if (null != commoditieAtrrs) {
            return commoditieAtrrs;
        }
        TCommodityAttrExample example = new TCommodityAttrExample();
        example.or().andCidEqualTo(cid);
        commoditieAtrrs = commodityAttrMapper.selectByExample(example);
        if (null != commoditieAtrrs) {
            redisTemplate.opsForValue().set(cacheName + cacheCommName + cid, commoditieAtrrs);
        }
        return commoditieAtrrs;
    }

    public boolean insert(TCommodityAttr row) {
        if (commodityAttrMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheCommName + row.getCid());
            return true;
        }
        return false;
    }

    public boolean update(TCommodityAttr row) {
        if (commodityAttrMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheCommName + row.getCid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TCommodityAttr commodityAttr = find(id);
        if (null == commodityAttr) {
            return false;
        }
        delCache(cacheCommName + commodityAttr.getCid());
        delCache(id);
        return commodityAttrMapper.deleteByPrimaryKey(id) > 0;
    }
}
