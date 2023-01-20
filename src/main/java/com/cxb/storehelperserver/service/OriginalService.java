package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
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
 * desc: 原料业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class OriginalService {
    @Resource
    private CheckService checkService;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private OriginalAttrRepository originalAttrRepository;

    @Resource
    private AttributeTemplateRepository attributeTemplateRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addOriginal(int id, TOriginal original, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, original.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 原料名重名检测
        if (originalRepository.checkCode(original.getGid(), original.getCode(), 0)) {
            return RestResult.fail("原料编号已存在");
        }
        if (originalRepository.checkName(original.getGid(), original.getName(), 0)) {
            return RestResult.fail("原料名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(original.getGid(), original.getAtid());
        if (null == attributeTemplates) {
            return RestResult.fail("原料属性模板信息异常");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("原料属性数量不匹配");
        }

        if (!originalRepository.insert(original)) {
            return RestResult.fail("添加原料信息失败");
        }

        if (!originalAttrRepository.update(original.getId(), attributes)) {
            return RestResult.fail("添加原料属性失败");
        }
        return RestResult.ok();
    }

    public RestResult setOriginal(int id, TOriginal original, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, original.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 原料名重名检测
        if (originalRepository.checkCode(original.getGid(), original.getCode(), original.getId())) {
            return RestResult.fail("原料编号已存在");
        }
        if (originalRepository.checkName(original.getGid(), original.getName(), original.getId())) {
            return RestResult.fail("原料名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(original.getGid(), original.getAtid());
        if (null == attributeTemplates) {
            return RestResult.fail("原料属性模板信息异常");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("原料属性数量不匹配");
        }

        if (!originalRepository.update(original)) {
            return RestResult.fail("修改原料信息失败");
        }

        if (!originalAttrRepository.update(original.getId(), attributes)) {
            return RestResult.fail("添加原料属性失败");
        }
        return RestResult.ok();
    }

    public RestResult delOriginal(int id, int oid) {
        TOriginal original = originalRepository.find(oid);
        if (null == original) {
            return RestResult.fail("要删除的原料不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, original.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!originalAttrRepository.delete(original.getId())) {
            return RestResult.fail("删除原料属性失败");
        }
        if (!originalRepository.delete(oid)) {
            return RestResult.fail("删除原料信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getOriginal(int id, int oid) {
        TOriginal original = originalRepository.find(oid);
        if (null == original) {
            return RestResult.fail("获取原料信息失败");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, original.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 属性
        val data = new HashMap<String, Object>();
        data.put("id", original.getId());
        data.put("code", original.getCode());
        data.put("name", original.getName());
        data.put("cid", original.getCid());
        data.put("atid", original.getAtid());
        data.put("price", original.getPrice().floatValue());
        data.put("remark", original.getRemark());
        List<TOriginalAttr> attrs = originalAttrRepository.find(original.getId());
        if (null != attrs && !attrs.isEmpty()) {
            val list = new ArrayList<String>();
            data.put("attrs", list);
            for (TOriginalAttr attr : attrs) {
                list.add(attr.getValue());
            }
        }
        return RestResult.ok(data);
    }

    public RestResult getGroupOriginal(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        int total = originalRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = originalRepository.pagination(group.getGid(), page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取原料信息异常");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TOriginal c : commodities) {
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
            List<TOriginalAttr> attrs = originalAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list = new ArrayList<String>();
                tmp.put("attrs", list);
                for (TOriginalAttr attr : attrs) {
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
