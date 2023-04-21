package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSaleTypeMapper;
import com.cxb.storehelperserver.model.TSaleType;
import com.cxb.storehelperserver.model.TSaleTypeExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 销售类型仓库
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Repository
public class SaleTypeRepository extends BaseRepository<TSaleType> {
    @Resource
    private TSaleTypeMapper saleTypeMapper;

    private final String cacheGroupName;

    public SaleTypeRepository() {
        init("saleType::");
        cacheGroupName = cacheName + "group::";
    }

    public TSaleType find(int id) {
        TSaleType saleType = getCache(id, TSaleType.class);
        if (null != saleType) {
            return saleType;
        }

        // 缓存没有就查询数据库
        saleType = saleTypeMapper.selectByPrimaryKey(id);
        if (null != saleType) {
            setCache(id, saleType);
        }
        return saleType;
    }

    public List<TSaleType> findByGroup(int gid) {
        List<TSaleType> types = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != types) {
            return types;
        }
        TSaleTypeExample example = new TSaleTypeExample();
        example.or().andGidEqualTo(gid);
        types = saleTypeMapper.selectByExample(example);
        if (null != types) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, types);
        }
        return types;
    }

    public boolean insert(TSaleType row) {
        if (saleTypeMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TSaleType row) {
        if (saleTypeMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TSaleType saleType = find(id);
        if (null == saleType) {
            return false;
        }
        delCache(cacheGroupName + saleType.getGid());
        delCache(id);
        return saleTypeMapper.deleteByPrimaryKey(id) > 0;
    }
}
