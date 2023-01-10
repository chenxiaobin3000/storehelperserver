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
 * desc: 商品业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CommodityService {
    @Resource
    private CheckService checkService;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private CommodityAttrRepository commodityAttrRepository;

    @Resource
    private AttributeTemplateRepository attributeTemplateRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addCommodity(int id, TCommodity commodity, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, commodity.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 商品名重名检测
        if (commodityRepository.checkCode(commodity.getGid(), commodity.getCode(), 0)) {
            return RestResult.fail("商品编号已存在");
        }
        if (commodityRepository.checkName(commodity.getGid(), commodity.getName(), 0)) {
            return RestResult.fail("商品名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(commodity.getGid(), commodity.getAtid());
        if (null == attributeTemplates) {
            return RestResult.fail("商品属性模板信息异常");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("商品属性数量不匹配");
        }

        if (!commodityRepository.insert(commodity)) {
            return RestResult.fail("添加商品信息失败");
        }

        if (!commodityAttrRepository.update(commodity.getId(), attributes)) {
            return RestResult.fail("添加商品属性失败");
        }
        return RestResult.ok();
    }

    public RestResult setCommodity(int id, TCommodity commodity, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, commodity.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 商品名重名检测
        if (commodityRepository.checkCode(commodity.getGid(), commodity.getCode(), commodity.getId())) {
            return RestResult.fail("商品编号已存在");
        }
        if (commodityRepository.checkName(commodity.getGid(), commodity.getName(), commodity.getId())) {
            return RestResult.fail("商品名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(commodity.getGid(), commodity.getAtid());
        if (null == attributeTemplates) {
            return RestResult.fail("商品属性模板信息异常");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("商品属性数量不匹配");
        }

        if (!commodityRepository.update(commodity)) {
            return RestResult.fail("修改商品信息失败");
        }

        if (!commodityAttrRepository.update(commodity.getId(), attributes)) {
            return RestResult.fail("添加商品属性失败");
        }
        return RestResult.ok();
    }

    public RestResult delCommodity(int id, int cid) {
        TCommodity commodity = commodityRepository.find(cid);
        if (null == commodity) {
            return RestResult.fail("要删除的商品不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, commodity.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!commodityRepository.delete(cid)) {
            return RestResult.fail("删除商品信息失败");
        }

        if (!commodityAttrRepository.delete(commodity.getId())) {
            return RestResult.fail("删除商品属性失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupCommodity(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        int total = commodityRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = commodityRepository.pagination(group.getGid(), page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息异常");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TCommodity c : commodities) {
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
            List<TCommodityAttr> attrs = commodityAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list = new ArrayList<String>();
                tmp.put("attrs", list);
                for (TCommodityAttr attr : attrs) {
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
