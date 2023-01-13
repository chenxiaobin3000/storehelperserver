package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TAttributeTemplate;
import com.cxb.storehelperserver.model.TDestroy;
import com.cxb.storehelperserver.model.TDestroyAttr;
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
 * desc: 废料业务
 * auth: cxb
 * date: 2023/1/14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DestroyService {
    @Resource
    private CheckService checkService;

    @Resource
    private DestroyRepository destroyRepository;

    @Resource
    private DestroyAttrRepository destroyAttrRepository;

    @Resource
    private AttributeTemplateRepository attributeTemplateRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addDestroy(int id, TDestroy destroy, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, destroy.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 废料名重名检测
        if (destroyRepository.checkCode(destroy.getGid(), destroy.getCode(), 0)) {
            return RestResult.fail("废料编号已存在");
        }
        if (destroyRepository.checkName(destroy.getGid(), destroy.getName(), 0)) {
            return RestResult.fail("废料名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(destroy.getGid(), destroy.getAtid());
        if (null == attributeTemplates) {
            return RestResult.fail("废料属性模板信息异常");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("废料属性数量不匹配");
        }

        if (!destroyRepository.insert(destroy)) {
            return RestResult.fail("添加废料信息失败");
        }

        if (!destroyAttrRepository.update(destroy.getId(), attributes)) {
            return RestResult.fail("添加废料属性失败");
        }
        return RestResult.ok();
    }

    public RestResult setDestroy(int id, TDestroy destroy, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, destroy.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 废料名重名检测
        if (destroyRepository.checkCode(destroy.getGid(), destroy.getCode(), destroy.getId())) {
            return RestResult.fail("废料编号已存在");
        }
        if (destroyRepository.checkName(destroy.getGid(), destroy.getName(), destroy.getId())) {
            return RestResult.fail("废料名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(destroy.getGid(), destroy.getAtid());
        if (null == attributeTemplates) {
            return RestResult.fail("废料属性模板信息异常");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("废料属性数量不匹配");
        }

        if (!destroyRepository.update(destroy)) {
            return RestResult.fail("修改废料信息失败");
        }

        if (!destroyAttrRepository.update(destroy.getId(), attributes)) {
            return RestResult.fail("添加废料属性失败");
        }
        return RestResult.ok();
    }

    public RestResult delDestroy(int id, int cid) {
        TDestroy destroy = destroyRepository.find(cid);
        if (null == destroy) {
            return RestResult.fail("要删除的废料不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, destroy.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!destroyAttrRepository.delete(destroy.getId())) {
            return RestResult.fail("删除废料属性失败");
        }
        if (!destroyRepository.delete(cid)) {
            return RestResult.fail("删除废料信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupDestroy(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        int total = destroyRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = destroyRepository.pagination(group.getGid(), page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取废料信息异常");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TDestroy c : commodities) {
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
            List<TDestroyAttr> attrs = destroyAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list = new ArrayList<String>();
                tmp.put("attrs", list);
                for (TDestroyAttr attr : attrs) {
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
