package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityMapper;
import com.cxb.storehelperserver.model.TCommodity;
import com.cxb.storehelperserver.model.TCommodityExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 商品仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class CommodityRepository extends BaseRepository<TCommodity> {
    @Resource
    private TCommodityMapper commodityMapper;

    private final String cacheGroupName;

    public CommodityRepository() {
        init("comm::");
        cacheGroupName = cacheName + "group::";
    }

    public TCommodity find(int id) {
        TCommodity commodity = getCache(id, TCommodity.class);
        if (null != commodity) {
            return commodity;
        }

        // 缓存没有就查询数据库
        commodity = commodityMapper.selectByPrimaryKey(id);
        if (null != commodity) {
            setCache(id, commodity);
        }
        return commodity;
    }

    public List<TCommodity> findByGroup(int gid) {
        List<TCommodity> commodities = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != commodities) {
            return commodities;
        }
        TCommodityExample example = new TCommodityExample();
        example.or().andGidEqualTo(gid);
        commodities = commodityMapper.selectByExample(example);
        if (null != commodities) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, commodities);
        }
        return commodities;
    }

    /*
     * desc: 判断公司是否存在品类
     */
    public boolean check(int gid, String name) {
        TCommodityExample example = new TCommodityExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        return null != commodityMapper.selectOneByExample(example);
    }

    public boolean insert(TCommodity row) {
        if (commodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TCommodity row) {
        if (commodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TCommodity role = find(id);
        if (null == role) {
            return false;
        }
        delCache(cacheGroupName + role.getGid());
        delCache(id);
        return commodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
