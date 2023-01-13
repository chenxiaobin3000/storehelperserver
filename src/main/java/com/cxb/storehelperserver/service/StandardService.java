package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TAttributeTemplate;
import com.cxb.storehelperserver.model.TStandard;
import com.cxb.storehelperserver.model.TStandardAttr;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.*;
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
 * desc: 标品业务
 * auth: cxb
 * date: 2023/1/14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StandardService {
    @Resource
    private CheckService checkService;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private StandardAttrRepository standardAttrRepository;

    @Resource
    private AttributeTemplateRepository attributeTemplateRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addStandard(int id, TStandard standard, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, standard.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 标品名重名检测
        if (standardRepository.checkCode(standard.getGid(), standard.getCode(), 0)) {
            return RestResult.fail("标品编号已存在");
        }
        if (standardRepository.checkName(standard.getGid(), standard.getName(), 0)) {
            return RestResult.fail("标品名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(standard.getGid(), standard.getAtid());
        if (null == attributeTemplates) {
            return RestResult.fail("标品属性模板信息异常");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("标品属性数量不匹配");
        }

        if (!standardRepository.insert(standard)) {
            return RestResult.fail("添加标品信息失败");
        }

        if (!standardAttrRepository.update(standard.getId(), attributes)) {
            return RestResult.fail("添加标品属性失败");
        }
        return RestResult.ok();
    }

    public RestResult setStandard(int id, TStandard standard, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, standard.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 标品名重名检测
        if (standardRepository.checkCode(standard.getGid(), standard.getCode(), standard.getId())) {
            return RestResult.fail("标品编号已存在");
        }
        if (standardRepository.checkName(standard.getGid(), standard.getName(), standard.getId())) {
            return RestResult.fail("标品名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(standard.getGid(), standard.getAtid());
        if (null == attributeTemplates) {
            return RestResult.fail("标品属性模板信息异常");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("标品属性数量不匹配");
        }

        if (!standardRepository.update(standard)) {
            return RestResult.fail("修改标品信息失败");
        }

        if (!standardAttrRepository.update(standard.getId(), attributes)) {
            return RestResult.fail("添加标品属性失败");
        }
        return RestResult.ok();
    }

    public RestResult delStandard(int id, int cid) {
        TStandard standard = standardRepository.find(cid);
        if (null == standard) {
            return RestResult.fail("要删除的标品不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, standard.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!standardAttrRepository.delete(standard.getId())) {
            return RestResult.fail("删除标品属性失败");
        }
        if (!standardRepository.delete(cid)) {
            return RestResult.fail("删除标品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupStandard(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        int total = standardRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = standardRepository.pagination(group.getGid(), page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取标品信息异常");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TStandard c : commodities) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("atid", c.getAtid());
            tmp.put("price", c.getPrice().floatValue());
            tmp.put("remark", c.getRemark());
            datas.add(tmp);

            // 属性
            List<TStandardAttr> attrs = standardAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list = new ArrayList<String>();
                tmp.put("attrs", list);
                for (TStandardAttr attr : attrs) {
                    list.add(attr.getValue());
                }
            }
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", datas);
        return RestResult.ok(data);
    }
}
