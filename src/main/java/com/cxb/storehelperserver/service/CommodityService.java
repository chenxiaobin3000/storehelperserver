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

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;

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
    private OriginalCommodityRepository originalCommodityRepository;

    @Resource
    private OriginalRepository originalRepository;

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
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(commodity.getGid(), CommodityType.COMMODITY.getValue());
        if (null == attributeTemplates) {
            return RestResult.fail("商品属性模板信息失败");
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
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(commodity.getGid(), CommodityType.COMMODITY.getValue());
        if (null == attributeTemplates) {
            return RestResult.fail("商品属性模板信息失败");
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

        if (!originalCommodityRepository.delete(commodity.getGid(), commodity.getId())) {
            return RestResult.fail("删除商品关联原料失败");
        }
        if (!commodityAttrRepository.delete(commodity.getId())) {
            return RestResult.fail("删除商品属性失败");
        }
        if (!commodityRepository.delete(cid)) {
            return RestResult.fail("删除商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getCommodity(int id, int cid) {
        TCommodity commodity = commodityRepository.find(cid);
        if (null == commodity) {
            return RestResult.fail("获取商品信息失败");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, commodity.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 属性
        val data = new HashMap<String, Object>();
        data.put("id", commodity.getId());
        data.put("code", commodity.getCode());
        data.put("name", commodity.getName());
        data.put("cid", commodity.getCid());
        data.put("unit", commodity.getUnit());
        data.put("remark", commodity.getRemark());
        List<TCommodityAttr> attrs = commodityAttrRepository.find(commodity.getId());
        if (null != attrs && !attrs.isEmpty()) {
            val list = new ArrayList<String>();
            data.put("attrs", list);
            for (TCommodityAttr attr : attrs) {
                list.add(attr.getValue());
            }
        }
        return RestResult.ok(data);
    }

    public RestResult getGroupCommodity(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int gid = group.getGid();
        int total = commodityRepository.total(gid, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = commodityRepository.pagination(gid, page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TCommodity c : commodities) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("unit", c.getUnit());
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

            // 关联来源
            TOriginalCommodity originalCommodity = originalCommodityRepository.find(gid, c.getId());
            if (null != originalCommodity) {
                TOriginal original = originalRepository.find(originalCommodity.getOid());
                if (null != original) {
                    tmp.put("oid", original.getId());
                    tmp.put("oname", original.getName());
                }
            }
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", datas);
        return RestResult.ok(data);
    }

    public RestResult getGroupAllCommodity(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int gid = group.getGid();
        int total = commodityRepository.total(gid, null);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = commodityRepository.pagination(gid, 1, total, null);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TCommodity c : commodities) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("unit", c.getUnit());
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

            // 关联来源
            TOriginalCommodity originalCommodity = originalCommodityRepository.find(gid, c.getId());
            if (null != originalCommodity) {
                TOriginal original = originalRepository.find(originalCommodity.getOid());
                if (null != original) {
                    tmp.put("oid", original.getId());
                    tmp.put("oname", original.getName());
                }
            }
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", datas);
        return RestResult.ok(data);
    }

    public RestResult setCommodityOriginal(int id, int gid, int cid, int oid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        TOriginalCommodity originalCommodity = originalCommodityRepository.find(gid, cid);
        if (null == originalCommodity) {
            originalCommodity = new TOriginalCommodity();
            originalCommodity.setGid(gid);
            originalCommodity.setOid(oid);
            originalCommodity.setCid(cid);
            if (!originalCommodityRepository.insert(originalCommodity)) {
                return RestResult.fail("添加商品关联原料失败");
            }
        } else {
            originalCommodity.setOid(oid);
            if (!originalCommodityRepository.update(originalCommodity)) {
                return RestResult.fail("修改商品关联原料失败");
            }
        }
        return RestResult.ok();
    }
}
