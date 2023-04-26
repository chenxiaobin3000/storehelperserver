package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAttributeTemplateMapper;
import com.cxb.storehelperserver.model.TAttributeTemplate;
import com.cxb.storehelperserver.model.TAttributeTemplateExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 属性模板仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class AttributeTemplateRepository extends BaseRepository<List> {
    @Resource
    private TAttributeTemplateMapper attributeTemplateMapper;

    public AttributeTemplateRepository() {
        init("attrTemp::");
    }

    public List<TAttributeTemplate> find(int gid) {
        List<TAttributeTemplate> templates = getCache(gid, List.class);
        if (null != templates) {
            return templates;
        }

        // 缓存没有就查询数据库
        TAttributeTemplateExample example = new TAttributeTemplateExample();
        example.or().andGidEqualTo(gid);
        example.setOrderByClause("idx asc");
        templates = attributeTemplateMapper.selectByExample(example);
        if (null != templates) {
            setCache(gid, templates);
        }
        return templates;
    }

    public boolean update(int gid, List<TAttributeTemplate> template) {
        // 清除属性 id 缓存
        delCache(gid);

        // 删除旧数据
        TAttributeTemplateExample example = new TAttributeTemplateExample();
        example.or().andGidEqualTo(gid);
        attributeTemplateMapper.deleteByExample(example);

        // 插入新数据
        for (TAttributeTemplate t : template) {
            if (attributeTemplateMapper.insert(t) < 0) {
                return false;
            }
        }
        return true;
    }
}
