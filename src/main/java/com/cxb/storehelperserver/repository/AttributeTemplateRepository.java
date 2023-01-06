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

    private final String cacheGroupName;

    public AttributeTemplateRepository() {
        init("attrTemp::");
        cacheGroupName = cacheName + "group::";
    }

    public List<TAttributeTemplate> find(int gid, int code) {
        List<TAttributeTemplate> templates = getCache(gid + "::" + code, List.class);
        if (null != templates) {
            return templates;
        }

        // 缓存没有就查询数据库
        TAttributeTemplateExample example = new TAttributeTemplateExample();
        example.or().andGidEqualTo(gid).andCodeEqualTo(code);
        example.setOrderByClause("idx asc");
        templates = attributeTemplateMapper.selectByExample(example);
        if (null != templates) {
            setCache(gid + "::" + code, templates);
        }
        return templates;
    }

    public List<List<TAttributeTemplate>> findByGroup(int gid) {
        List<List<TAttributeTemplate>> templates = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != templates) {
            return templates;
        }
        TAttributeTemplateExample example = new TAttributeTemplateExample();
        example.or().andGidEqualTo(gid);
        example.setOrderByClause("idx asc");
        List<TAttributeTemplate> rets = attributeTemplateMapper.selectByExample(example);
        if (null != rets) {
            val template1 = new ArrayList<TAttributeTemplate>();
            val template2 = new ArrayList<TAttributeTemplate>();
            val template3 = new ArrayList<TAttributeTemplate>();
            val template4 = new ArrayList<TAttributeTemplate>();
            val template5 = new ArrayList<TAttributeTemplate>();
            for (TAttributeTemplate t : rets) {
                switch (t.getCode()) {
                    case 1:
                        template1.add(t);
                        break;
                    case 2:
                        template2.add(t);
                        break;
                    case 3:
                        template3.add(t);
                        break;
                    case 4:
                        template4.add(t);
                        break;
                    default:
                        template5.add(t);
                        break;
                }
            }
            templates = new ArrayList<>();
            templates.add(template1);
            templates.add(template2);
            templates.add(template3);
            templates.add(template4);
            templates.add(template5);
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, templates);
        }
        return templates;
    }

    public boolean update(int gid, List<List<TAttributeTemplate>> templates) {
        // 清除属性 id 缓存
        delCache(gid + "::1");
        delCache(gid + "::2");
        delCache(gid + "::3");
        delCache(gid + "::4");
        delCache(gid + "::5");

        // 删除旧数据
        TAttributeTemplateExample example = new TAttributeTemplateExample();
        example.or().andGidEqualTo(gid);
        attributeTemplateMapper.deleteByExample(example);

        // 插入新数据
        int code = 0;
        TAttributeTemplate attributeTemplate = new TAttributeTemplate();
        for (List<TAttributeTemplate> template : templates) {
            code = code + 1;
            for (TAttributeTemplate t : template) {
                attributeTemplate.setId(0);
                attributeTemplate.setGid(gid);
                attributeTemplate.setCode(code);
                attributeTemplate.setAid(t.getAid());
                attributeTemplate.setIdx(t.getIdx());
                if (attributeTemplateMapper.insert(attributeTemplate) < 0) {
                    return false;
                }
            }
        }
        redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, templates);
        return true;
    }
}
