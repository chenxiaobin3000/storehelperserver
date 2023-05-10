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
    private MarketCommodityRepository marketCommodityRepository;

    @Resource
    private MarketAccountRepository marketAccountRepository;

    @Resource
    private AttributeTemplateRepository attributeTemplateRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addCommodityList(int id, int gid, List<String> codes, List<String> names, List<Integer> cids, List<String> remarks, List<String> storages, int attrNum,
                                       List<String> attr1, List<String> attr2, List<String> attr3, List<String> attr4, List<String> attr5, List<String> attr6, List<String> attr7, List<String> attr8) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        int size = codes.size();
        if (size != names.size() || size != cids.size() || size != remarks.size() || size != attr1.size()) {
            return RestResult.fail("导入信息异常");
        }
        if ((attrNum > 2 && size != attr2.size()) || (attrNum > 3 && size != attr3.size()) || (attrNum > 4 && size != attr4.size())
                || (attrNum > 5 && size != attr5.size()) || (attrNum > 6 && size != attr6.size()) || (attrNum > 7 && size != attr7.size()) || (attrNum > 8 && size != attr8.size())) {
            return RestResult.fail("导入信息异常");
        }

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(gid);
        if (null == attributeTemplates) {
            return RestResult.fail("商品属性模板信息失败");
        }

        List<String> attributes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String code = codes.get(i);
            TCommodity commodity = new TCommodity();
            commodity.setGid(gid);
            commodity.setCode(code);
            commodity.setName(names.get(i));
            commodity.setCid(cids.get(i));
            commodity.setRemark(remarks.get(i));

            // 商品名重名检测
            if (commodityRepository.checkCode(gid, code, 0)) {
                return RestResult.fail("商品编号已存在: " + names.get(i));
            }
            if (!commodityRepository.insert(commodity)) {
                return RestResult.fail("添加商品信息失败");
            }

            // 属性
            attributes.clear();
            attributes.add(attr1.get(i));
            if (attrNum > 1) {
                attributes.add(attr2.get(i));
                if (attrNum > 2) {
                    attributes.add(attr3.get(i));
                    if (attrNum > 3) {
                        attributes.add(attr4.get(i));
                        if (attrNum > 4) {
                            attributes.add(attr5.get(i));
                            if (attrNum > 5) {
                                attributes.add(attr6.get(i));
                                if (attrNum > 6) {
                                    attributes.add(attr7.get(i));
                                    if (attrNum > 7) {
                                        attributes.add(attr8.get(i));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!commodityAttrRepository.update(commodity.getId(), attributes)) {
                return RestResult.fail("添加商品属性失败");
            }

            // 关联仓库
            String ss = storages.get(i);
            if (!ss.isEmpty()) {
                String[] list = ss.split(",");
                val sids = new ArrayList<Integer>();
                for (String s : list) {
                    int sid = Integer.parseInt(s);
                    if (null == storageRepository.find(sid)) {
                        return RestResult.fail("未查询到仓库信息:" + names.get(i));
                    }
                    sids.add(sid);
                }
                if (!commodityStorageRepository.update(commodity.getId(), sids)) {
                    return RestResult.fail("添加商品关联仓库失败:" + names.get(i));
                }
            }
        }
        return RestResult.ok();
    }

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

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(commodity.getGid());
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

        // 检测属性数量是否匹配
        List<TAttributeTemplate> attributeTemplates = attributeTemplateRepository.find(commodity.getGid());
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
        commodityOriginalRepository.delete(cid);
        commodityAttrRepository.delete(cid);
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
            datas.add(createCommodity(c));
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
            val tmp = createCommodity(c);
            datas.add(tmp);

            // 库存
            TStockDay stock = stockService.getStockCommodity(gid, sid, c.getId());
            if (null != stock) {
                tmp.put("sprice", stock.getPrice());
                tmp.put("sweight", stock.getWeight());
                tmp.put("snorm", stock.getNorm());
                tmp.put("svalue", stock.getValue());
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult setCommodityOriginal(int id, int gid, int cid, String oid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        TCommodity commodity = commodityRepository.findByCode(oid);
        if (null == commodity) {
            return RestResult.fail("未查询到关联商品信息");
        }

        TCommodityOriginal commodityOriginal = commodityOriginalRepository.find(cid);
        if (null == commodityOriginal) {
            commodityOriginal = new TCommodityOriginal();
            commodityOriginal.setOid(commodity.getId());
            commodityOriginal.setCid(cid);
            if (!commodityOriginalRepository.insert(commodityOriginal)) {
                return RestResult.fail("添加商品关联原料失败");
            }
        } else {
            commodityOriginal.setOid(commodity.getId());
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

    private HashMap<String, Object> createCommodity(TCommodity c) {
        int cid = c.getId();
        val tmp = new HashMap<String, Object>();
        tmp.put("id", cid);
        tmp.put("code", c.getCode());
        tmp.put("name", c.getName());
        tmp.put("cid", c.getCid());
        tmp.put("remark", c.getRemark());

        // 仓库
        val storages = commodityStorageRepository.find(cid);
        if (null != storages && !storages.isEmpty()) {
            val list = new ArrayList<HashMap<String, Object>>();
            tmp.put("storages", list);
            for (TCommodityStorage ss : storages) {
                val tmp2 = new HashMap<String, Object>();
                int sid = ss.getSid();
                tmp2.put("sid", sid);
                TStorage storage = storageRepository.find(sid);
                if (null != storage) {
                    tmp2.put("name", storage.getName());
                }
                list.add(tmp2);
            }
        }

        // 属性
        List<TCommodityAttr> attrs = commodityAttrRepository.find(cid);
        if (null != attrs && !attrs.isEmpty()) {
            val list = new ArrayList<String>();
            tmp.put("attrs", list);
            for (TCommodityAttr attr : attrs) {
                list.add(attr.getValue());
            }
        }

        // 关联来源
        TCommodityOriginal commodityOriginal = commodityOriginalRepository.find(cid);
        if (null != commodityOriginal) {
            TCommodity original = commodityRepository.find(commodityOriginal.getOid());
            if (null != original) {
                tmp.put("oname", original.getName());
                tmp.put("ocode", original.getCode());
            }
        }

        // 账号
        val list = marketCommodityRepository.findByCid(cid);
        if (null != list && !list.isEmpty()) {
            val list2 = new ArrayList<String>();
            for (TMarketCommodity commodity : list) {
                TMarketAccount account = marketAccountRepository.find(commodity.getAid());
                if (null != account) {
                    list2.add(account.getAccount());
                }
            }
            tmp.put("accounts", list2);
        }
        return tmp;
    }
}
