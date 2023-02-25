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
import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction.*;

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
    private StoragePurchaseRepository storagePurchaseRepository;

    @Resource
    private StorageDispatchRepository storageDispatchRepository;

    @Resource
    private StorageFareRepository storageFareRepository;

    @Resource
    private StorageReturnRepository storageReturnRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseCommodityRepository purchaseCommodityRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 仓储采购入库
     */
    public RestResult purchase(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase_apply, mp_storage_purchase_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchaseComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 仓储采购入库修改
     */
    public RestResult setPurchase(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase_apply, mp_storage_purchase_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        int oid = order.getId();
        TStorageOrder storageOrder = storageOrderRepository.find(oid);
        if (null == storageOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != storageOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!storageOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(order.getOtype(), oid, order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchaseComms(order, order.getOid(), types, commoditys, values, comms);
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
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewPurchase(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验所有入库单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        if (!storagePurchaseRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加采购入库信息失败");
        }

        // 增加库存
        if (stockService.addStock(id, true, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid)) {
            return RestResult.fail("生成库存信息失败");
        }
        return reviewService.review(id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), mp_storage_purchase_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        if (!storagePurchaseRepository.delete(order.getId(), order.getOid())) {
            return RestResult.fail("撤销采购入库信息失败");
        }

        // 减少库存
        if (stockService.addStock(id, false, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid)) {
            return RestResult.fail("撤销库存信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 仓储调度出库
     */
    public RestResult dispatch(int id, TStorageOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_dispatch_apply, mp_storage_dispatch_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成调度订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!storageFareRepository.insert(oid, fare)) {
                return RestResult.fail("添加调度物流费用失败");
            }
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 仓储调度出库修改
     */
    public RestResult setDispatch(int id, TStorageOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_dispatch_apply, mp_storage_dispatch_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        int oid = order.getId();
        TStorageOrder storageOrder = storageOrderRepository.find(oid);
        if (null == storageOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != storageOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!storageOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(order.getOtype(), oid, order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成调度订单失败");
        }
        String msg = storageOrderService.update(order.getId(), comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (fare.compareTo(BigDecimal.ZERO) > 0) {
            storageFareRepository.delete(oid);
            if (!storageFareRepository.insert(oid, fare)) {
                return RestResult.fail("添加调度物流费用失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delDispatch(int id, int oid) {
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

        // TODO 删运费
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewDispatch(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验所有入库单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 增加库存
        if (stockService.addStock(id, false, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid)) {
            return RestResult.fail("生成库存信息失败");
        }
        // 财务记录
        val fares = storageFareRepository.find(oid);
        for (TStorageFare fare : fares) {
            if (!financeService.insertRecord(id, group.getGid(), FINANCE_STORAGE_FARE, order.getId(), fare.getFare().negate())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return reviewService.review(id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeDispatch(int id, int oid) {
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), mp_storage_dispatch_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        if (stockService.addStock(id, true, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid)) {
            return RestResult.fail("撤销库存信息失败");
        }
        // 财务记录
        val fares = storageFareRepository.find(oid);
        for (TStorageFare fare : fares) {
            if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_FARE, order.getId(), fare.getFare())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 仓储调度入库
     */
    public RestResult purchase2(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase2_apply, mp_storage_purchase2_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 调度单未审核不能入库
        int rid = order.getOid();
        TStorageOrder storageOrder = storageOrderRepository.find(rid);
        if (null == storageOrder) {
            return RestResult.fail("未查询到采购单");
        }
        if (null == storageOrder.getReview()) {
            return RestResult.fail("调度单未审核通过，不能进行入库");
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 仓储调度入库修改
     */
    public RestResult setPurchase2(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase2_apply, mp_storage_purchase2_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        int oid = order.getId();
        TStorageOrder storageOrder = storageOrderRepository.find(oid);
        if (null == storageOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != storageOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!storageOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(order.getOtype(), oid, order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, comms);
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

    public RestResult delPurchase2(int id, int oid) {
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
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewPurchase2(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验所有入库单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        if (!storageDispatchRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加调度入库信息失败");
        }

        // 增加库存
        if (stockService.addStock(id, true, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid)) {
            return RestResult.fail("生成库存信息失败");
        }
        return reviewService.review(id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokePurchase2(int id, int oid) {
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), mp_storage_purchase2_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        if (!storageDispatchRepository.delete(order.getId(), order.getOid())) {
            return RestResult.fail("撤销调度入库信息失败");
        }

        // 减少库存
        if (stockService.addStock(id, false, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid)) {
            return RestResult.fail("撤销库存信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 仓储损耗
     */
    public RestResult loss(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_loss_apply, mp_storage_loss_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 仓储损耗修改
     */
    public RestResult setLoss(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_loss_apply, mp_storage_loss_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        int oid = order.getId();
        TStorageOrder storageOrder = storageOrderRepository.find(oid);
        if (null == storageOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != storageOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!storageOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(order.getOtype(), oid, order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageComms(order, types, commoditys, values, comms);
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

    public RestResult delLoss(int id, int oid) {
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
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewLoss(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验所有入库单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 增加库存
        if (stockService.addStock(id, true, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid)) {
            return RestResult.fail("生成库存信息失败");
        }
        return reviewService.review(id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeLoss(int id, int oid) {
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), mp_storage_loss_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        if (stockService.addStock(id, false, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid)) {
            return RestResult.fail("撤销库存信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 仓储退货
     */
    public RestResult returnc(int id, TStorageOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_return_apply, mp_storage_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 入库单未审核不能退货
        int rid = order.getOid();
        TStorageOrder storageOrder = storageOrderRepository.find(rid);
        if (null == storageOrder) {
            return RestResult.fail("未查询到采购单");
        }
        if (null == storageOrder.getReview()) {
            return RestResult.fail("入库单未审核通过，不能进行退货");
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createReturnComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成退货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }

        // 插入订单商品和附件数据
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!storageFareRepository.insert(oid, fare)) {
                return RestResult.fail("添加退货物流费用失败");
            }
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 仓储退货修改
     */
    public RestResult setReturn(int id, TStorageOrder order, BigDecimal fare, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_return_apply, mp_storage_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        int oid = order.getId();
        TStorageOrder storageOrder = storageOrderRepository.find(oid);
        if (null == storageOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != storageOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!storageOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(order.getOtype(), oid, order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成退货单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createReturnComms(order, order.getOid(), types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 插入订单商品和附件数据
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 运费
        if (fare.compareTo(BigDecimal.ZERO) > 0) {
            storageFareRepository.delete(oid);
            if (!storageFareRepository.insert(oid, fare)) {
                return RestResult.fail("添加退货物流费用失败");
            }
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
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewReturn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // TODO 校验所有退货单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 修改对应进货单数据
        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getOid());
        if (null == purchase) {
            return RestResult.fail("未查询到对应的进货单");
        }
        purchase.setCurUnit(purchase.getCurUnit() - order.getUnit());
        purchase.setCurPrice(purchase.getCurPrice().subtract(order.getPrice()));
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改进货单数据失败");
        }

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!storageReturnRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // TODO 减少库存

        // 财务记录
        if (!financeService.insertRecord(id, group.getGid(), FINANCE_STORAGE_RET, order.getId(), order.getPrice())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = storageFareRepository.find(oid);
        for (TStorageFare fare : fares) {
            if (!financeService.insertRecord(id, group.getGid(), FINANCE_STORAGE_FARE2, order.getId(), fare.getFare().negate())) {
                return RestResult.fail("添加运费记录失败");
            }
        }
        return reviewService.review(id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), mp_storage_return_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!storageReturnRepository.delete(oid)) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // TODO 增加库存

        // 财务记录
        BigDecimal money = purchaseCommodityRepository.count(oid);
        if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_RET, order.getId(), money.negate())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = storageFareRepository.find(oid);
        for (TStorageFare fare : fares) {
            if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_FARE2, order.getId(), fare.getFare())) {
                return RestResult.fail("添加运费记录失败");
            }
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

    private RestResult createPurchaseComms(TStorageOrder order, int pid, List<Integer> types, List<Integer> commoditys,
                                           List<Integer> values, List<TStorageCommodity> list) {
        // 生成入库单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息出错");
        }
        val purchaseCommodities = purchaseCommodityRepository.find(pid);
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            boolean find = false;
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            for (TPurchaseCommodity pc : purchaseCommodities) {
                if (pc.getCtype() == ctype && pc.getCid() == cid) {
                    find = true;
                    // 生成数据
                    TStorageCommodity c = new TStorageCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    c.setValue(values.get(i));
                    list.add(c);

                    // 校验商品入库数不能大于采购单
                    if (values.get(i) > pc.getValue()) {
                        return RestResult.fail("入库商品数量不能大于采购数量, 商品id:" + cid + ", 类型:" + ctype);
                    }

                    total = total + pc.getUnit() * values.get(i);
                    price = price.add(pc.getPrice());
                    break;
                }
            }
            if (!find) {
                return RestResult.fail("未查询到商品id:" + cid + ", 类型:" + ctype);
            }
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }

    private RestResult createStorageComms(TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                                          List<Integer> values, List<TStorageCommodity> list) {
        // 生成入库单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息出错");
        }
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            TypeDefine.CommodityType type = TypeDefine.CommodityType.valueOf(types.get(i));
            int cid = commoditys.get(i);
            switch (type) {
                case ORIGINAL:
                    if (null == originalRepository.find(cid)) {
                        return RestResult.fail("未查询到原料：" + cid);
                    }
                    break;
                case STANDARD:
                    if (null == standardRepository.find(cid)) {
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

    private RestResult createReturnComms(TStorageOrder order, int rid, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TStorageCommodity> list) {
        // 生成退货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息出错");
        }
        val purchaseCommodities = purchaseCommodityRepository.find(rid);
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            boolean find = false;
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            for (TPurchaseCommodity pc : purchaseCommodities) {
                if (pc.getCtype() == ctype && pc.getCid() == cid) {
                    find = true;
                    // 生成数据
                    TStorageCommodity c = new TStorageCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    c.setValue(values.get(i));
                    list.add(c);

                    // 校验商品退货数不能大于采购单
                    if (values.get(i) > pc.getValue()) {
                        return RestResult.fail("退货商品数量不能大于采购数量, 商品id:" + cid + ", 类型:" + ctype);
                    }

                    total = total + pc.getUnit() * values.get(i);
                    price = price.add(pc.getPrice());
                    break;
                }
            }
            if (!find) {
                return RestResult.fail("未查询到商品id:" + cid + ", 类型:" + ctype);
            }
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }
}
