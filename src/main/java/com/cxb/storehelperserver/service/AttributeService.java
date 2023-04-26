package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TAttribute;
import com.cxb.storehelperserver.model.TAttributeTemplate;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.AttributeRepository;
import com.cxb.storehelperserver.repository.AttributeTemplateRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * desc: 属性业务
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttributeService {
    @Resource
    private CheckService checkService;

    @Resource
    private AttributeRepository attributeRepository;

    @Resource
    private AttributeTemplateRepository attributeTemplateRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addAttribute(int id, TAttribute attribute) {
        // 验证公司
        String msg = checkService.checkGroup(id, attribute.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 属性名重名检测
        if (attributeRepository.check(attribute.getGid(), attribute.getName(), 0)) {
            return RestResult.fail("属性名称已存在");
        }

        if (!attributeRepository.insert(attribute)) {
            return RestResult.fail("添加属性信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setAttribute(int id, TAttribute attribute) {
        // 验证公司
        String msg = checkService.checkGroup(id, attribute.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 属性名重名检测
        if (attributeRepository.check(attribute.getGid(), attribute.getName(), attribute.getId())) {
            return RestResult.fail("属性名称已存在");
        }

        if (!attributeRepository.update(attribute)) {
            return RestResult.fail("修改属性信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delAttribute(int id, int cid) {
        TAttribute attribute = attributeRepository.find(cid);
        if (null == attribute) {
            return RestResult.fail("要删除的属性不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, attribute.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // TODO 是否有商品在使用该属性

        if (!attributeRepository.delete(cid)) {
            return RestResult.fail("删除属性信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupAttribute(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        List<TAttribute> attributes = attributeRepository.findByGroup(group.getGid());
        if (null == attributes) {
            return RestResult.fail("获取属性信息失败");
        }

        val data = new HashMap<String, Object>();
        data.put("list", attributes);
        return RestResult.ok(data);
    }

    public RestResult updateAttributeTemplate(int id, int gid, List<String> template) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 获取公司所有属性
        List<TAttribute> attributes = attributeRepository.findByGroup(gid);
        if (null == attributes) {
            return RestResult.fail("获取属性信息失败");
        }
        if (!attributeTemplateRepository.update(gid, name2Temp(gid, template, attributes))) {
            return RestResult.fail("修改属性模板信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupAttributeTemplate(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 获取公司所有属性
        List<TAttribute> attributes = attributeRepository.findByGroup(group.getGid());
        if (null == attributes) {
            return RestResult.fail("获取属性信息失败");
        }

        val data = new HashMap<String, Object>();
        List<TAttributeTemplate> attrTemp = attributeTemplateRepository.find(group.getGid());
        if (null == attrTemp) {
            return RestResult.fail("获取属性模板信息失败");
        }
        data.put("list", temp2Name(attrTemp, attributes));
        return RestResult.ok(data);
    }

    private List<TAttributeTemplate> name2Temp(int gid, List<String> template, List<TAttribute> attributes) {
        val list = new ArrayList<TAttributeTemplate>();
        int index = 0;
        for (String name : template) {
            for (TAttribute attribute : attributes) {
                if (name.equals(attribute.getName())) {
                    TAttributeTemplate tmp = new TAttributeTemplate();
                    tmp.setGid(gid);
                    tmp.setAid(attribute.getId());
                    tmp.setIdx(++index);
                    list.add(tmp);
                    break;
                }
            }
        }
        return list;
    }

    private List<String> temp2Name(List<TAttributeTemplate> template, List<TAttribute> attributes) {
        if (null == template) {
            return null;
        }
        val tmp = new ArrayList<String>();
        for (TAttributeTemplate t : template) {
            for (TAttribute attribute : attributes) {
                if (t.getAid().equals(attribute.getId())) {
                    tmp.add(attribute.getName());
                    break;
                }
            }
        }
        return tmp;
    }
}
