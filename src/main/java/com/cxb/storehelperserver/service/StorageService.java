package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.TypeDefine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.*;

/**
 * desc: 仓库订单业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageService {
    @Resource
    private CheckService checkService;

    @Resource
    private StorageOrderService storageOrderService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private FinanceService financeService;

    @Resource
    private StockService stockService;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;

    @Resource
    private StorageAttachmentRepository storageAttachmentRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 原料入库
     */
    public RestResult purchase(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                               List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_in_apply, mp_storage_in_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
        String batch = dateUtil.createBatch(String.valueOf(STORAGE_IN_ORDER.getValue()));
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        return reviewService.apply(id, order.getGid(), order.getSid(), STORAGE_IN_ORDER.getValue(), oid, batch, reviews);
    }

    /**
     * desc: 原料入库修改
     */
    public RestResult setPurchase(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                                  List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_in_apply, mp_storage_in_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        TStorageOrder storageOrder = storageOrderRepository.find(order.getId());
        if (null == storageOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != storageOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!storageOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(STORAGE_IN_ORDER.getValue(), order.getId(), order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        String msg = storageOrderService.update(order.getId(), comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delPurchase(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 校验是否订单提交人，已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("订单必须由申请人删除");
        }

        // 删除生效日期以后的所有库存记录，删除日期是制单日期的前一天
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(order.getApplyTime());
        calendar.add(Calendar.DATE, -1);
        stockService.delStock(order.getSid(), calendar.getTime());

        // 删除商品附件数据
        if (!storageCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!storageAttachmentRepository.delete(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!storageOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, STORAGE_IN_ORDER.getValue(), oid);
    }

    public RestResult reviewPurchase(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        if (!reviewService.checkReview(id, STORAGE_IN_ORDER.getValue(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验采购单和入库数量

        // 添加审核信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        if (!purchaseStorageRepository.insert(order.getRid(), order.getId())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // TODO 添加库存
        return reviewService.review(id, order.getGid(), order.getSid(),
                STORAGE_IN_ORDER.getValue(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokePurchase(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, storage_purchase)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), STORAGE_IN_ORDER.getValue(), oid, order.getBatch(), mp_storage_in_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        if (!purchaseStorageRepository.delete(order.getRid(), order.getId())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // TODO 减少库存
        return RestResult.ok();
    }

    /**
     * desc: 原料退货
     */
    public RestResult returnc(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                              List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_out_apply, mp_storage_out_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 入库单未审核不能删除
        int rid = order.getOid();
        TPurchaseOrder purchase = purchaseOrderRepository.find(rid);
        if (null == purchase) {
            return RestResult.fail("未查询到采购单");
        }
        if (null == purchase.getReview()) {
            return RestResult.fail("未审批的采购单请直接删除");
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
        String batch = dateUtil.createBatch(String.valueOf(STORAGE_OUT_ORDER.getValue()));
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成出库订单失败");
        }

        // TODO 校验入库单商品数不能大于退货数

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        return reviewService.apply(id, order.getGid(), order.getSid(), STORAGE_OUT_ORDER.getValue(), oid, batch, reviews);
    }

    /**
     * desc: 原料退货修改
     */
    public RestResult setReturn(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                                List<Integer> values, List<BigDecimal> prices, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_out_apply, mp_storage_out_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        TStorageOrder storageOrder = storageOrderRepository.find(order.getId());
        if (null == storageOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != storageOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!storageOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(STORAGE_OUT_ORDER.getValue(), order.getId(), order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, prices, comms);
        if (null != ret) {
            return ret;
        }

        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成入库订单失败");
        }

        // TODO 校验入库单商品数不能大于退货数

        // 插入订单商品和附件数据
        String msg = storageOrderService.update(order.getId(), comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delReturn(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 校验是否订单提交人，已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("订单必须由申请人删除");
        }

        // 删除商品附件数据
        if (!storageCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!storageAttachmentRepository.delete(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!storageOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, STORAGE_OUT_ORDER.getValue(), oid);
    }

    public RestResult reviewReturn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        if (!reviewService.checkReview(id, STORAGE_OUT_ORDER.getValue(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 添加审核信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        if (!purchaseStorageRepository.insert(order.getRid(), order.getId())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // TODO 减少库存
        return reviewService.review(id, order.getGid(), order.getSid(),
                STORAGE_OUT_ORDER.getValue(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, storage_return)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), STORAGE_OUT_ORDER.getValue(), oid, order.getBatch(), mp_storage_out_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // TODO 删除
        if (!purchaseStorageRepository.delete(order.getRid(), order.getId())) {
            return RestResult.fail("添加采购退货信息失败");
        }
        return RestResult.ok();
    }

    public List<MyOrderCommodity> getOrderCommodity(int id, int gid, int oid) {
        return null;
    }

    private RestResult check(int id, TStorageOrder order, int applyPerm, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(id, gid, applyPerm, reviewPerm, reviews);
    }

    private RestResult createStorageComms(List<Integer> types, List<Integer> commoditys,
                                          List<Integer> values, List<BigDecimal> prices, List<TStorageCommodity> list) {
        // 生成入库单
        int size = commoditys.size();
        if (size != types.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            TypeDefine.CommodityType type = TypeDefine.CommodityType.valueOf(types.get(i));
            int cid = commoditys.get(i);
            switch (type) {
                case ORIGINAL:
                    TOriginal original = originalRepository.find(cid);
                    if (null == original) {
                        return RestResult.fail("未查询到原料：" + cid);
                    }
                    break;
                case STANDARD:
                    TStandard standard = standardRepository.find(cid);
                    if (null == standard) {
                        return RestResult.fail("未查询到标品：" + cid);
                    }
                    break;
                default:
                    return RestResult.fail("商品类型异常：" + type);
            }

            // 生成数据
            TStorageCommodity c = new TStorageCommodity();
            c.setCtype(type.getValue());
            c.setCid(cid);
            c.setValue(values.get(i));
            list.add(c);
        }
        return null;
    }
}
