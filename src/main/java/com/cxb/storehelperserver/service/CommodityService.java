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
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.COMMODITY;

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
    private StockService stockService;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private CommodityAttrRepository commodityAttrRepository;

    @Resource
    private CommodityOriginalRepository commodityOriginalRepository;

    @Resource
    private CommodityStorageRepository commodityStorageRepository;

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

        commodityStorageRepository.delete(cid);
        if (!commodityOriginalRepository.delete(cid)) {
            return RestResult.fail("删除商品关联原料失败");
        }
        if (!commodityAttrRepository.delete(cid)) {
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
            return RestResult.ok(new PageData());
        }

        val commodities = commodityRepository.pagination(gid, page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TCommodity c : commodities) {
            int cid = c.getId();
            val tmp = new HashMap<String, Object>();
            tmp.put("id", cid);
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
            tmp.put("remark", c.getRemark());
            datas.add(tmp);

            // 仓库
            val storages = commodityStorageRepository.find(cid);
            if (null != storages && !storages.isEmpty()) {
                val list2 = new ArrayList<HashMap<String, Object>>();
                tmp.put("storages", list2);
                for (TCommodityStorage ss : storages) {
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
            List<TCommodityAttr> attrs = commodityAttrRepository.find(c.getId());
            if (null != attrs && !attrs.isEmpty()) {
                val list = new ArrayList<String>();
                tmp.put("attrs", list);
                for (TCommodityAttr attr : attrs) {
                    list.add(attr.getValue());
                }
            }

            // 关联来源
            TCommodityOriginal commodityOriginal = commodityOriginalRepository.find(c.getId());
            if (null != commodityOriginal) {
                TOriginal original = originalRepository.find(commodityOriginal.getOid());
                if (null != original) {
                    tmp.put("oid", original.getId());
                    tmp.put("oname", original.getName());
                }
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult getStorageCommodity(int id, int sid, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();
        TStorage storage = storageRepository.find(sid);
        if (null == storage) {
            return RestResult.fail("获取仓库信息失败");
        }
        if (!storage.getGid().equals(gid)) {
            return RestResult.fail("只能获取本公司信息");
        }

        int total = commodityStorageRepository.total(sid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val commodities = commodityStorageRepository.pagination(sid, page, limit, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        for (TCommodity c : commodities) {
            int cid = c.getId();
            val tmp = new HashMap<String, Object>();
            tmp.put("id", cid);
            tmp.put("code", c.getCode());
            tmp.put("name", c.getName());
            tmp.put("cid", c.getCid());
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

            // 库存
            TStockDay stock = stockService.getStockCommodity(gid, sid, COMMODITY.getValue(), cid);
            if (null != stock) {
                tmp.put("sprice", stock.getPrice());
                tmp.put("sweight", stock.getWeight());
                tmp.put("svalue", stock.getValue());
            }

            // 关联来源
            TCommodityOriginal commodityOriginal = commodityOriginalRepository.find(c.getId());
            if (null != commodityOriginal) {
                TOriginal original = originalRepository.find(commodityOriginal.getOid());
                if (null != original) {
                    tmp.put("oid", original.getId());
                    tmp.put("oname", original.getName());
                }
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult setCommodityOriginal(int id, int gid, int cid, int oid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        TCommodityOriginal commodityOriginal = commodityOriginalRepository.find(cid);
        if (null == commodityOriginal) {
            commodityOriginal = new TCommodityOriginal();
            commodityOriginal.setOid(oid);
            commodityOriginal.setCid(cid);
            if (!commodityOriginalRepository.insert(commodityOriginal)) {
                return RestResult.fail("添加商品关联原料失败");
            }
        } else {
            commodityOriginal.setOid(oid);
            if (!commodityOriginalRepository.update(commodityOriginal)) {
                return RestResult.fail("修改商品关联原料失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult setCommodityStorage(int id, int gid, int cid, List<Integer> sids) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!commodityStorageRepository.update(cid, sids)) {
            return RestResult.fail("添加商品关联仓库失败");
        }
        return RestResult.ok();
    }
}
