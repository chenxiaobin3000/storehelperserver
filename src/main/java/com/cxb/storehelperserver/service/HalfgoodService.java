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
 * desc: 半成品业务
 * auth: cxb
 * date: 2023/1/14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class HalfgoodService {
    @Resource
    private CheckService checkService;

    @Resource
    private HalfgoodRepository halfgoodRepository;

    @Resource
    private HalfgoodAttrRepository halfgoodAttrRepository;

    @Resource
    private OriginalHalfgoodRepository originalHalfgoodRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private AttributeTemplateRepository attributeTemplateRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addHalfgood(int id, THalfgood halfgood, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, halfgood.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 半成品名重名检测
        if (halfgoodRepository.checkCode(halfgood.getGid(), halfgood.getCode(), 0)) {
            return RestResult.fail("半成品编号已存在");
        }
        if (halfgoodRepository.checkName(halfgood.getGid(), halfgood.getName(), 0)) {
            return RestResult.fail("半成品名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(halfgood.getGid(), CommodityType.HALFGOOD.getValue());
        if (null == attributeTemplates) {
            return RestResult.fail("半成品属性模板信息失败");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("半成品属性数量不匹配");
        }

        if (!halfgoodRepository.insert(halfgood)) {
            return RestResult.fail("添加半成品信息失败");
        }

        if (!halfgoodAttrRepository.update(halfgood.getId(), attributes)) {
            return RestResult.fail("添加半成品属性失败");
        }
        return RestResult.ok();
    }

    public RestResult setHalfgood(int id, THalfgood halfgood, List<String> attributes) {
        // 验证公司
        String msg = checkService.checkGroup(id, halfgood.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 半成品名重名检测
        if (halfgoodRepository.checkCode(halfgood.getGid(), halfgood.getCode(), halfgood.getId())) {
            return RestResult.fail("半成品编号已存在");
        }
        if (halfgoodRepository.checkName(halfgood.getGid(), halfgood.getName(), halfgood.getId())) {
            return RestResult.fail("半成品名称已存在");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(halfgood.getGid(), CommodityType.HALFGOOD.getValue());
        if (null == attributeTemplates) {
            return RestResult.fail("半成品属性模板信息失败");
        }
        if (null == attributes || attributes.isEmpty() || attributeTemplates.size() != attributes.size()) {
            return RestResult.fail("半成品属性数量不匹配");
        }

        if (!halfgoodRepository.update(halfgood)) {
            return RestResult.fail("修改半成品信息失败");
        }

        if (!halfgoodAttrRepository.update(halfgood.getId(), attributes)) {
            return RestResult.fail("添加半成品属性失败");
        }
        return RestResult.ok();
    }

    public RestResult delHalfgood(int id, int hid) {
        THalfgood halfgood = halfgoodRepository.find(hid);
        if (null == halfgood) {
            return RestResult.fail("要删除的半成品不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, halfgood.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!originalHalfgoodRepository.delete(halfgood.getGid(), halfgood.getId())) {
            return RestResult.fail("删除半成品关联原料失败");
        }
        if (!halfgoodAttrRepository.delete(halfgood.getId())) {
            return RestResult.fail("删除半成品属性失败");
        }
        if (!halfgoodRepository.delete(hid)) {
            return RestResult.fail("删除半成品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getHalfgood(int id, int hid) {
        THalfgood halfgood = halfgoodRepository.find(hid);
        if (null == halfgood) {
            return RestResult.fail("获取半成品信息失败");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, halfgood.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 属性
        val data = new HashMap<String, Object>();
        data.put("id", halfgood.getId());
        data.put("code", halfgood.getCode());
        data.put("name", halfgood.getName());
        data.put("cid", halfgood.getCid());
        data.put("price", halfgood.getPrice().floatValue());
        data.put("unit", halfgood.getUnit());
        data.put("remark", halfgood.getRemark());
        List<THalfgoodAttr> attrs = halfgoodAttrRepository.find(halfgood.getId());
        if (null != attrs && !attrs.isEmpty()) {
            val list = new ArrayList<String>();
            data.put("attrs", list);
            for (THalfgoodAttr attr : attrs) {
                list.add(attr.getValue());
            }
        }
        return RestResult.ok(data);
    }

    public RestResult getGroupHalfgood(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int gid = group.getGid();
        int total = halfgoodRepository.total(gid, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = halfgoodRepository.pagination(gid, page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取半成品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (THalfgood c : commodities) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("price", c.getPrice().floatValue());
            tmp.put("unit", c.getUnit());
            tmp.put("remark", c.getRemark());
            datas.add(tmp);

            // 属性
            List<THalfgoodAttr> attrs = halfgoodAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list = new ArrayList<String>();
                tmp.put("attrs", list);
                for (THalfgoodAttr attr : attrs) {
                    list.add(attr.getValue());
                }
            }

            // 关联来源
            TOriginalHalfgood originalHalfgood = originalHalfgoodRepository.find(gid, c.getId());
            if (null != originalHalfgood) {
                TOriginal original = originalRepository.find(originalHalfgood.getOid());
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

    public RestResult getGroupAllHalfgood(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int gid = group.getGid();
        int total = halfgoodRepository.total(gid, null);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = halfgoodRepository.pagination(gid, 1, total, null);
        if (null == commodities) {
            return RestResult.fail("获取半成品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (THalfgood c : commodities) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("price", c.getPrice().floatValue());
            tmp.put("unit", c.getUnit());
            tmp.put("remark", c.getRemark());
            datas.add(tmp);

            // 属性
            List<THalfgoodAttr> attrs = halfgoodAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list = new ArrayList<String>();
                tmp.put("attrs", list);
                for (THalfgoodAttr attr : attrs) {
                    list.add(attr.getValue());
                }
            }

            // 关联来源
            TOriginalHalfgood originalHalfgood = originalHalfgoodRepository.find(gid, c.getId());
            if (null != originalHalfgood) {
                TOriginal original = originalRepository.find(originalHalfgood.getOid());
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

    public RestResult setHalfgoodOriginal(int id, int gid, int hid, int oid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        TOriginalHalfgood originalHalfgood = originalHalfgoodRepository.find(gid, hid);
        if (null == originalHalfgood) {
            originalHalfgood = new TOriginalHalfgood();
            originalHalfgood.setGid(gid);
            originalHalfgood.setOid(oid);
            originalHalfgood.setHid(hid);
            if (!originalHalfgoodRepository.insert(originalHalfgood)) {
                return RestResult.fail("添加商品关联半成品失败");
            }
        } else {
            originalHalfgood.setOid(oid);
            if (!originalHalfgoodRepository.update(originalHalfgood)) {
                return RestResult.fail("修改商品关联半成品失败");
            }
        }
        return RestResult.ok();
    }
}
