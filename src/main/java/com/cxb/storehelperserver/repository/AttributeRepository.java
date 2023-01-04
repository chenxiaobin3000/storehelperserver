package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAttributeMapper;
import com.cxb.storehelperserver.model.TAttribute;
import com.cxb.storehelperserver.model.TAttributeExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 属性仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class AttributeRepository extends BaseRepository<TAttribute> {
    @Resource
    private TAttributeMapper attributeMapper;

    private final String cacheGroupName;

    public AttributeRepository() {
        init("comm::");
        cacheGroupName = cacheName + "group::";
    }

    public TAttribute find(int id) {
        TAttribute attribute = getCache(id, TAttribute.class);
        if (null != attribute) {
            return attribute;
        }

        // 缓存没有就查询数据库
        attribute = attributeMapper.selectByPrimaryKey(id);
        if (null != attribute) {
            setCache(id, attribute);
        }
        return attribute;
    }

    public List<TAttribute> findByGroup(int gid) {
        List<TAttribute> attributes = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != attributes) {
            return attributes;
        }
        TAttributeExample example = new TAttributeExample();
        example.or().andGidEqualTo(gid);
        attributes = attributeMapper.selectByExample(example);
        if (null != attributes) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, attributes);
        }
        return attributes;
    }

    /*
     * desc: 判断公司是否存在属性
     */
    public boolean check(int gid, String name) {
        TAttributeExample example = new TAttributeExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        return null != attributeMapper.selectOneByExample(example);
    }

    public boolean insert(TAttribute row) {
        if (attributeMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TAttribute row) {
        if (attributeMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TAttribute role = find(id);
        if (null == role) {
            return false;
        }
        delCache(cacheGroupName + role.getGid());
        delCache(id);
        return attributeMapper.deleteByPrimaryKey(id) > 0;
    }
}
