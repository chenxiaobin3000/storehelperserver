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
    private StandardCloudRepository standardCloudRepository;

    @Resource
    private StandardStorageRepository standardStorageRepository;

    @Resource
    private AttributeTemplateRepository attributeTemplateRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private CloudRepository cloudRepository;

    @Resource
    private StorageRepository storageRepository;

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
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(standard.getGid(), CommodityType.STANDARD.getValue());
        if (null == attributeTemplates) {
            return RestResult.fail("标品属性模板信息失败");
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
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(standard.getGid(), CommodityType.STANDARD.getValue());
        if (null == attributeTemplates) {
            return RestResult.fail("标品属性模板信息失败");
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

    public RestResult delStandard(int id, int sid) {
        TStandard standard = standardRepository.find(sid);
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
        if (!standardRepository.delete(sid)) {
            return RestResult.fail("删除标品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getStandard(int id, int sid) {
        TStandard standard = standardRepository.find(sid);
        if (null == standard) {
            return RestResult.fail("获取标品信息失败");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, standard.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 属性
        val data = new HashMap<String, Object>();
        data.put("id", standard.getId());
        data.put("code", standard.getCode());
        data.put("name", standard.getName());
        data.put("cid", standard.getCid());
        data.put("remark", standard.getRemark());
        List<TStandardAttr> attrs = standardAttrRepository.find(standard.getId());
        if (null != attrs && !attrs.isEmpty()) {
            val list = new ArrayList<String>();
            data.put("attrs", list);
            for (TStandardAttr attr : attrs) {
                list.add(attr.getValue());
            }
        }
        return RestResult.ok(data);
    }

    public RestResult getGroupStandard(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = standardRepository.total(group.getGid(), search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = standardRepository.pagination(group.getGid(), page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取标品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TStandard c : commodities) {
            int cid = c.getId();
            val tmp = new HashMap<String, Object>();
            tmp.put("id", cid);
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("remark", c.getRemark());
            datas.add(tmp);

            // 云仓
            val clouds = standardCloudRepository.find(cid);
            if (null != clouds && !clouds.isEmpty()) {
                val list2 = new ArrayList<HashMap<String, Object>>();
                tmp.put("clouds", list2);
                for (TStandardCloud sc : clouds) {
                    val tmp2 = new HashMap<String, Object>();
                    int sid = sc.getSid();
                    tmp2.put("sid", sid);
                    TCloud cloud = cloudRepository.find(sid);
                    if (null != cloud) {
                        tmp2.put("name", cloud.getName());
                    }
                    list2.add(tmp2);
                }
            }

            // 仓库
            val storages = standardStorageRepository.find(cid);
            if (null != storages && !storages.isEmpty()) {
                val list2 = new ArrayList<HashMap<String, Object>>();
                tmp.put("storages", list2);
                for (TStandardStorage ss : storages) {
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
            val attrs = standardAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list = new ArrayList<String>();
                tmp.put("attrs", list);
                for (TStandardAttr attr : attrs) {
                    list.add(attr.getValue());
                }
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult getGroupAllStandard(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        int total = standardRepository.total(group.getGid(), null);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = standardRepository.pagination(group.getGid(), 1, total, null);
        if (null == commodities) {
            return RestResult.fail("获取标品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TStandard c : commodities) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
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
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult setStandardCloud(int id, int gid, int cid, List<Integer> sids) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!standardCloudRepository.update(cid, sids)) {
            return RestResult.fail("添加标品关联云仓失败");
        }
        return RestResult.ok();
    }

    public RestResult setStandardStorage(int id, int gid, int cid, List<Integer> sids) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!standardStorageRepository.update(cid, sids)) {
            return RestResult.fail("添加标品关联仓库失败");
        }
        return RestResult.ok();
    }
}
