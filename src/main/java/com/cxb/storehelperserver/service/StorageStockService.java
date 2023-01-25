package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.StorageCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType;

/**
 * desc: 仓库库存业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageStockService extends StorageCache {
    @Resource
    private CheckService checkService;

    @Resource
    private StorageCacheService storageCacheService;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;

    @Resource
    private StorageHalfgoodRepository storageHalfgoodRepository;

    @Resource
    private StorageOriginalRepository storageOriginalRepository;

    @Resource
    private StorageStandardRepository storageStandardRepository;

    @Resource
    private StorageDestroyRepository storageDestroyRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private OrderReviewerRepository orderReviewerRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private HalfgoodRepository halfgoodRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private DateUtil dateUtil;

    /*
     * desc: 原料进货
     */
    public RestResult purchase(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                               List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 验证审核人员信息
        val orderReviewers = orderReviewerRepository.find(gid);
        if (null == orderReviewers || orderReviewers.isEmpty()) {
            return RestResult.fail("未设置订单审核人，请联系系统管理员");
        }
        val reviews = new ArrayList<Integer>();
        for (TOrderReviewer orderReviewer : orderReviewers) {
            if (orderReviewer.getPid().equals(OrderType.STORAGE_IN_ORDER.getValue())) {
                reviews.add(orderReviewer.getUid());
            }
        }
        if (reviews.isEmpty()) {
            return RestResult.fail("未设置进货订单审核人，请联系系统管理员");
        }

        // 生成进货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        int total = 0;
        val list = new ArrayList<TStorageOrderCommodity>();
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            CommodityType type = CommodityType.valueOf(types.get(i));
            int cid = commoditys.get(i);
            int unit = 0;
            switch (type) {
                case COMMODITY:
                    TCommodity find1 = commodityRepository.find(cid);
                    if (null == find1) {
                        return RestResult.fail("未查询到商品：" + cid);
                    }
                    unit = find1.getUnit();
                    break;
                case HALFGOOD:
                    THalfgood find2 = halfgoodRepository.find(cid);
                    if (null == find2) {
                        return RestResult.fail("未查询到半成品：" + cid);
                    }
                    unit = find2.getUnit();
                    break;
                case ORIGINAL:
                    TOriginal find3 = originalRepository.find(cid);
                    if (null == find3) {
                        return RestResult.fail("未查询到原料：" + cid);
                    }
                    unit = find3.getUnit();
                    break;
                case STANDARD:
                    TStandard find4 = standardRepository.find(cid);
                    if (null == find4) {
                        return RestResult.fail("未查询到标品：" + cid);
                    }
                    unit = find4.getUnit();
                    break;
            }

            // 生成数据
            TStorageOrderCommodity c = new TStorageOrderCommodity();
            c.setCtype(type.getValue());
            c.setCid(cid);
            c.setUnit(unit);
            c.setValue(values.get(i));
            c.setPrice(prices.get(i));
            list.add(c);
            total += values.get(i);
        }
        order.setValue(total);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成进货订单失败");
        }
        int oid = order.getId();
        int sid = order.getSid();
        // 修改库存数量
        for (TStorageOrderCommodity c : list) {
            c.setOid(oid);
            switch (CommodityType.valueOf(c.getCtype())) {
                case COMMODITY:
                    if (!setCommodityCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                case HALFGOOD:
                    if (!setHalfgoodCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改半成品库存数据失败");
                    }
                    break;
                case ORIGINAL:
                    if (!setOriginalCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改原料库存数据失败");
                    }
                    break;
                case STANDARD:
                    if (!setStandardCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改标品库存数据失败");
                    }
                    break;
                default:
                    if (!setDestroyCache(sid, c.getId(), c.getUnit() * c.getValue())) {
                        return RestResult.fail("修改废料库存数据失败");
                    }
                    break;
            }
        }
        msg = storageCacheService.update(oid, list, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加用户订单冗余信息
        String batch = order.getBatch();
        TUserOrderApply userOrderApply = new TUserOrderApply();
        userOrderApply.setUid(id);
        userOrderApply.setOtype(OrderType.STORAGE_IN_ORDER.getValue());
        userOrderApply.setOid(oid);
        userOrderApply.setBatch(batch);
        if (!userOrderApplyRepository.insert(userOrderApply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview userOrderReview = new TUserOrderReview();
        userOrderReview.setOtype(OrderType.STORAGE_IN_ORDER.getValue());
        userOrderReview.setOid(oid);
        userOrderReview.setBatch(batch);
        for (Integer reviewer : reviews) {
            userOrderReview.setId(0);
            userOrderReview.setUid(reviewer);
        }
        return RestResult.ok();
    }

    /*
     * desc: 生产开始
     */
    public RestResult process(int id, int gid, int sid, List<Integer> commoditys, List<Integer> values, List<String> prices) {
        return RestResult.ok();
    }

    /*
     * desc: 生产完成
     */
    public RestResult complete(int id, int gid, int sid, List<Integer> commoditys, List<Integer> values, List<String> prices) {
        return RestResult.ok();
    }

    public RestResult getStorageOrder(int id, int page, int limit, String search) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        int total = storageOrderRepository.total(group.getGid(), search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        // 查询联系人
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val list2 = new ArrayList<>();
        val list = storageOrderRepository.pagination(group.getGid(), page, limit, search);
        if (null != list && !list.isEmpty()) {
            for (TStorageOrder o : list) {
                val storage = new HashMap<String, Object>();
                storage.put("id", o.getId());
                storage.put("batch", o.getBatch());
                storage.put("value", o.getValue());

                TStorage s = storageRepository.find(o.getSid());
                if (null != s) {
                    storage.put("sid", s.getId());
                    storage.put("sname", s.getName());
                }

                TUser ua = userRepository.find(o.getApply());
                if (null != ua) {
                    storage.put("apply", ua.getId());
                    storage.put("applyName", ua.getName());
                }
                storage.put("applyTime", dateFormat.format(o.getApplyTime()));

                Integer review = o.getReview();
                if (null != review) {
                    TUser uv = userRepository.find(review);
                    if (null != uv) {
                        storage.put("review", uv.getId());
                        storage.put("reviewName", uv.getName());
                    }
                    storage.put("reviewTime", dateFormat.format(o.getReviewTime()));
                }

                HashMap<String, Object> datas = storageCacheService.find(o.getId());
                if (null != datas) {
                    storage.put("comms", datas.get("comms"));
                    storage.put("attrs", datas.get("attrs"));
                }
                list2.add(storage);
            }
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list2);
        return RestResult.ok(data);
    }
}
