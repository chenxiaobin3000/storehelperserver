package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityMapper;
import com.cxb.storehelperserver.model.TCommodity;
import com.cxb.storehelperserver.model.TCommodityExample;
import com.cxb.storehelperserver.model.TGroupExample;
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

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TCommodityExample example = new TCommodityExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) commodityMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TCommodityExample example = new TCommodityExample();
            example.or().andGidEqualTo(gid);
            total = (int) commodityMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TCommodity> pagination(int gid, int page, int limit, String search) {
        List<TCommodity> commodities = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != commodities) {
            return commodities;
        }
        TCommodityExample example = new TCommodityExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
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
        TCommodity commodity = find(id);
        if (null == commodity) {
            return false;
        }
        delCache(cacheGroupName + commodity.getGid());
        delCache(id);
        return commodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
