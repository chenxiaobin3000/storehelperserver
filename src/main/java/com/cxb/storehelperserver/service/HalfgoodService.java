package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.service.model.PageData;
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
    private HalfgoodOriginalRepository halfgoodOriginalRepository;

    @Resource
    private HalfgoodStorageRepository halfgoodStorageRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private AttributeTemplateRepository attributeTemplateRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private StorageRepository storageRepository;

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

        halfgoodStorageRepository.delete(hid);
        if (!halfgoodOriginalRepository.delete(hid)) {
            return RestResult.fail("删除半成品关联原料失败");
        }
        if (!halfgoodAttrRepository.delete(hid)) {
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
            return RestResult.ok(new PageData());
        }

        val commodities = halfgoodRepository.pagination(gid, page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取半成品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (THalfgood c : commodities) {
            int cid = c.getId();
            val tmp = new HashMap<String, Object>();
            tmp.put("id", cid);
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("remark", c.getRemark());
            datas.add(tmp);

            // 仓库
            val storages = halfgoodStorageRepository.find(cid);
            if (null != storages && !storages.isEmpty()) {
                val list2 = new ArrayList<HashMap<String, Object>>();
                tmp.put("storages", list2);
                for (THalfgoodStorage ss : storages) {
                    val tmp2 = new HashMap<String, Object>();
                    int sid = ss.getSid();
                    tmp2.put("sid", sid);
                    TStorage storage = storageRepository.find(sid);
                    if (null != storage) {
                        tmp2.put("name", storage.getName());
                    }
                    list2.add(tmp2);
                }
            }

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
            THalfgoodOriginal halfgoodOriginal = halfgoodOriginalRepository.find(c.getId());
            if (null != halfgoodOriginal) {
                TOriginal original = originalRepository.find(halfgoodOriginal.getOid());
                if (null != original) {
                    tmp.put("oid", original.getId());
                    tmp.put("oname", original.getName());
                }
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult getStorageHalfgood(int id, int sid, int page, int limit, String search) {
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        TStorage storage = storageRepository.find(sid);
        if (null == storage) {
            return RestResult.fail("获取仓库信息失败");
        }
        if (!group.getGid().equals(storage.getGid())) {
            return RestResult.fail("只能获取本公司信息");
        }

        int gid = group.getGid();
        int total = halfgoodStorageRepository.total(sid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = halfgoodStorageRepository.pagination(sid, page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取半成品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (THalfgood c : commodities) {
            int cid = c.getId();
            val tmp = new HashMap<String, Object>();
            tmp.put("id", cid);
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
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
            THalfgoodOriginal halfgoodOriginal = halfgoodOriginalRepository.find(c.getId());
            if (null != halfgoodOriginal) {
                TOriginal original = originalRepository.find(halfgoodOriginal.getOid());
                if (null != original) {
                    tmp.put("oid", original.getId());
                    tmp.put("oname", original.getName());
                }
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult setHalfgoodOriginal(int id, int gid, int hid, int oid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        THalfgoodOriginal halfgoodOriginal = halfgoodOriginalRepository.find(hid);
        if (null == halfgoodOriginal) {
            halfgoodOriginal = new THalfgoodOriginal();
            halfgoodOriginal.setOid(oid);
            halfgoodOriginal.setHid(hid);
            if (!halfgoodOriginalRepository.insert(halfgoodOriginal)) {
                return RestResult.fail("添加商品关联半成品失败");
            }
        } else {
            halfgoodOriginal.setOid(oid);
            if (!halfgoodOriginalRepository.update(halfgoodOriginal)) {
                return RestResult.fail("修改商品关联半成品失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult setHalfgoodStorage(int id, int gid, int cid, List<Integer> sids) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!halfgoodStorageRepository.update(cid, sids)) {
            return RestResult.fail("添加商品关联仓库失败");
        }
        return RestResult.ok();
    }
}
