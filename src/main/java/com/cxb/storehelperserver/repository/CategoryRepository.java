package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCategoryMapper;
import com.cxb.storehelperserver.model.TCategory;
import com.cxb.storehelperserver.model.TCategoryExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 品类仓库
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Repository
public class CategoryRepository extends BaseRepository<TCategory> {
    @Resource
    private TCategoryMapper categoryMapper;

    private final String cacheGroupName;

    public CategoryRepository() {
        init("cate::");
        cacheGroupName = cacheName + "group::";
    }

    public TCategory find(int id) {
        TCategory category = getCache(id, TCategory.class);
        if (null != category) {
            return category;
        }

        // 缓存没有就查询数据库
        category = categoryMapper.selectByPrimaryKey(id);
        if (null != category) {
            setCache(id, category);
        }
        return category;
    }

    public List<TCategory> findAll() {
        List<TCategory> categories = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName));
        if (null != categories) {
            return categories;
        }
        TCategoryExample example = new TCategoryExample();
        example.setOrderByClause("level asc");
        categories = categoryMapper.selectByExample(example);
        if (null != categories) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName, categories);
        }
        return categories;
    }

    /*
     * desc: 判断公司是否存在品类
     */
    public boolean check(String name, int id) {
        TCategoryExample example = new TCategoryExample();
        example.or().andNameEqualTo(name);
        if (0 == id) {
            return null != categoryMapper.selectOneByExample(example);
        } else {
            TCategory category = categoryMapper.selectOneByExample(example);
            return null != category && !category.getId().equals(id);
        }
    }

    public boolean insert(TCategory row) {
        if (categoryMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName);
            return true;
        }
        return false;
    }

    public boolean update(TCategory row) {
        if (categoryMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TCategory category = find(id);
        if (null == category) {
            return false;
        }
        delCache(cacheGroupName);
        delCache(id);
        return categoryMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean checkChildren(int id) {
        TCategoryExample example = new TCategoryExample();
        example.or().andParentEqualTo(id);
        TCategory category = categoryMapper.selectOneByExample(example);
        return null != category;
    }
}
