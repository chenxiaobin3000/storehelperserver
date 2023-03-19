package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction.*;
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
    private StorageStockService storageStockService;

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
    private StorageAgreementRepository storageAgreementRepository;

    @Resource
    private StorageFareRepository storageFareRepository;

    @Resource
    private StorageRemarkRepository storageRemarkRepository;

    @Resource
    private StorageReturnRepository storageReturnRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseCommodityRepository purchaseCommodityRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private StockRepository stockRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 仓储采购入库
     */
    public RestResult purchase(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 采购单未审核不能入库
        int rid = order.getOid();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(rid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到采购单");
        }
        if (!purchaseOrder.getOtype().equals(PURCHASE_PURCHASE_ORDER.getValue())) {
            return RestResult.fail("进货单据类型异常");
        }
        if (null == purchaseOrder.getReview()) {
            return RestResult.fail("采购单未审核通过，不能进行入库");
        }

        order.setGid(purchaseOrder.getGid());
        order.setSid(purchaseOrder.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase_apply, mp_storage_purchase_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchaseComms(order, order.getOid(), types, commoditys, weights, values, comms);
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
    public RestResult setPurchase(int id, int oid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase_apply, mp_storage_purchase_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchaseComms(order, order.getOid(), types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
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
        storageAttachmentRepository.deleteByOid(oid);
        if (!storageCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
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

        // 校验入库总量不能超出采购单
        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getOid());
        if (null == purchase) {
            return RestResult.fail("未查询到对应的进货单");
        }
        int unit = purchase.getCurUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("入库商品总量不能超出采购订单总量");
        }
        if (0 == unit) {
            purchase.setComplete(new Byte("1"));
        }
        purchase.setCurUnit(unit);
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改进货单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!storagePurchaseRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加采购入库信息失败");
        }

        // 增加库存
        String msg = storageStockService.handlePurchaseStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokePurchase(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
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

        // TODO 还原扣除的采购单数量

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_purchase_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!storagePurchaseRepository.delete(oid, order.getOid())) {
            return RestResult.fail("撤销采购入库信息失败");
        }

        // 减少库存
        msg = storageStockService.handlePurchaseStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addPurchaseInfo(int id, int oid, String remark) {
        // 验证公司
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能由申请人添加信息");
        }
        storageOrderService.clean(oid);

        // 备注
        if (null != remark && remark.length() > 0) {
            if (!storageRemarkRepository.insert(oid, remark, new Date())) {
                return RestResult.fail("添加备注失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delPurchaseInfo(int id, int oid, int rid) {
        // 验证公司
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        storageOrderService.clean(oid);

        // 备注由审核人删
        if (0 != rid) {
            if (!order.getReview().equals(rid)) {
                RestResult.fail("要删除备注，请联系订单审核人");
            }
            if (!storageRemarkRepository.delete(rid)) {
                return RestResult.fail("删除备注信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 仓储调度出库
     */
    public RestResult dispatch(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_dispatch_apply, mp_storage_dispatch_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createDispatchComms(order, types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成调度订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 仓储调度出库修改
     */
    public RestResult setDispatch(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_dispatch_apply, mp_storage_dispatch_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 更新仓库信息
        if (!order.getSid().equals(sid)) {
            order.setSid(sid);
            ret = reviewService.update(order.getOtype(), oid, sid);
            if (null != ret) {
                return ret;
            }
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createDispatchComms(order, types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成调度订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
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
        storageAttachmentRepository.deleteByOid(oid);
        if (!storageCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!storageOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewDispatch(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = storageStockService.handleStorageStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        // 财务记录
        val fares = storageFareRepository.findByOid(oid);
        if (null != fares && !fares.isEmpty()) {
            for (TStorageFare fare : fares) {
                if (null == fare.getReview()) {
                    fare.setReview(id);
                    fare.setReviewTime(reviewTime);
                    if (!storageFareRepository.update(fare)) {
                        return RestResult.fail("更新运费信息失败");
                    }
                    if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_FARE, oid, fare.getFare().negate())) {
                        return RestResult.fail("添加运费记录失败");
                    }
                }
            }
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeDispatch(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, storage_dispatch)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_dispatch_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = storageStockService.handleStorageStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        // 财务记录
        val fares = storageFareRepository.findByOid(oid);
        if (null != fares && !fares.isEmpty()) {
            for (TStorageFare fare : fares) {
                if (null != fare.getReview()) {
                    if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_FARE, oid, fare.getFare())) {
                        return RestResult.fail("添加运费记录失败");
                    }
                }
            }
            if (!storageFareRepository.setReviewNull(oid)) {
                return RestResult.fail("更新运费信息失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult addDispatchInfo(int id, int oid, BigDecimal fare, String remark) {
        // 验证公司
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能由申请人添加信息");
        }
        storageOrderService.clean(oid);

        // 运费
        if (null != fare && fare.compareTo(BigDecimal.ZERO) > 0) {
            if (!storageFareRepository.insert(oid, fare, new Date())) {
                return RestResult.fail("添加物流费用失败");
            }
        }

        // 备注
        if (null != remark && remark.length() > 0) {
            if (!storageRemarkRepository.insert(oid, remark, new Date())) {
                return RestResult.fail("添加备注失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delDispatchInfo(int id, int oid, int fid, int rid) {
        // 验证公司
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        storageOrderService.clean(oid);

        // 运费由申请人删，已审核由审核人删，备注由审核人删
        if (0 != fid) {
            TStorageFare fare = storageFareRepository.find(fid);
            if (null == fare) {
                return RestResult.fail("未查询到运费信息");
            }
            if (null != fare.getReview()) {
                if (!fare.getReview().equals(id)) {
                    return RestResult.fail("要删除已审核信息，请联系审核人");
                }
            } else {
                if (!order.getApply().equals(id)) {
                    return RestResult.fail("只能由申请人删除信息");
                }
            }
            if (!storageFareRepository.delete(fid)) {
                return RestResult.fail("删除运费信息失败");
            }
        }

        // 备注由审核人删
        if (0 != rid) {
            if (!order.getReview().equals(rid)) {
                RestResult.fail("要删除备注，请联系订单审核人");
            }
            if (!storageRemarkRepository.delete(rid)) {
                return RestResult.fail("删除备注信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 仓储调度入库
     */
    public RestResult purchase2(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 调度单未审核不能入库
        int did = order.getOid();
        TStorageOrder dispatch = storageOrderRepository.find(did);
        if (null == dispatch) {
            return RestResult.fail("未查询到调度单");
        }
        if (!dispatch.getOtype().equals(STORAGE_DISPATCH_ORDER.getValue())) {
            return RestResult.fail("进货单据类型异常");
        }
        if (null == dispatch.getReview()) {
            return RestResult.fail("调度单未审核通过，不能进行入库");
        }

        order.setGid(dispatch.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase2_apply, mp_storage_purchase2_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchase2Comms(order, did, types, commoditys, weights, values, comms);
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
    public RestResult setPurchase2(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase2_apply, mp_storage_purchase2_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 更新仓库信息
        if (!order.getSid().equals(sid)) {
            order.setSid(sid);
            ret = reviewService.update(order.getOtype(), oid, sid);
            if (null != ret) {
                return ret;
            }
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchase2Comms(order, order.getOid(), types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
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
        storageAttachmentRepository.deleteByOid(oid);
        if (!storageCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
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
        int gid = group.getGid();

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出采购单
        TStorageOrder dispatch = storageOrderRepository.find(order.getOid());
        if (null == dispatch) {
            return RestResult.fail("未查询到对应的调度单");
        }
        int unit = dispatch.getCurUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("入库商品总量不能超出调度订单总量");
        }
        if (0 == unit) {
            dispatch.setComplete(new Byte("1"));
        }
        dispatch.setCurUnit(unit);
        if (!storageOrderRepository.update(dispatch)) {
            return RestResult.fail("修改调度单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!storageDispatchRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加调度入库信息失败");
        }

        // 增加库存
        String msg = storageStockService.handlePurchaseStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokePurchase2(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, storage_purchase2)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // TODO 还原扣除的采购单数量

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_purchase2_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!storageDispatchRepository.delete(oid, order.getOid())) {
            return RestResult.fail("撤销调度入库信息失败");
        }

        // 减少库存
        msg = storageStockService.handlePurchaseStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addPurchase2Info(int id, int oid, String remark) {
        return addPurchaseInfo(id, oid, remark);
    }

    public RestResult delPurchase2Info(int id, int oid, int rid) {
        return delPurchaseInfo(id, oid, rid);
    }

    /**
     * desc: 仓储履约入库
     */
    public RestResult agreement(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 调度单未审核不能入库
        int did = order.getOid();
        TAgreementOrder agreement = agreementOrderRepository.find(did);
        if (null == agreement) {
            return RestResult.fail("未查询到履约单");
        }
        if (!agreement.getOtype().equals(CLOUD_BACK_ORDER.getValue())) {
            return RestResult.fail("退货单据类型异常");
        }
        if (null == agreement.getReview()) {
            return RestResult.fail("履约单未审核通过，不能进行入库");
        }

        order.setGid(agreement.getGid());
        order.setSid(agreement.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_agreement_apply, mp_storage_agreement_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createAgreementComms(order, did, types, commoditys, weights, values, comms);
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
     * desc: 仓储履约入库修改
     */
    public RestResult setAgreement(int id, int oid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_agreement_apply, mp_storage_agreement_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createAgreementComms(order, order.getOid(), types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成入库订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delAgreement(int id, int oid) {
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
        storageAttachmentRepository.deleteByOid(oid);
        if (!storageCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!storageOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewAgreement(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出履约单
        TAgreementOrder agreement = agreementOrderRepository.find(order.getOid());
        if (null == agreement) {
            return RestResult.fail("未查询到对应的履约单");
        }
        int unit = agreement.getCurUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("入库商品总量不能超出履约订单总量");
        }
        if (0 == unit) {
            agreement.setComplete(new Byte("1"));
        }
        agreement.setCurUnit(unit);
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改履约单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!storageAgreementRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加履约入库信息失败");
        }

        // 增加库存
        String msg = storageStockService.handlePurchaseStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeAgreement(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, storage_agreement)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // TODO 还原扣除的采购单数量

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_agreement_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!storageDispatchRepository.delete(oid, order.getOid())) {
            return RestResult.fail("撤销调度入库信息失败");
        }

        // 减少库存
        msg = storageStockService.handlePurchaseStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addAgreementInfo(int id, int oid, String remark) {
        return addPurchaseInfo(id, oid, remark);
    }

    public RestResult delAgreementInfo(int id, int oid, int rid) {
        return delPurchaseInfo(id, oid, rid);
    }

    /**
     * desc: 仓储损耗
     */
    public RestResult loss(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_loss_apply, mp_storage_loss_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createDispatchComms(order, types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成损耗订单失败");
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
    public RestResult setLoss(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_loss_apply, mp_storage_loss_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 更新仓库信息
        if (!order.getSid().equals(sid)) {
            order.setSid(sid);
            ret = reviewService.update(order.getOtype(), oid, sid);
            if (null != ret) {
                return ret;
            }
        }

        // 生成损耗单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createDispatchComms(order, types, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成损耗订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
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
        storageAttachmentRepository.deleteByOid(oid);
        if (!storageCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
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
        int gid = group.getGid();

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = storageStockService.handleStorageStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeLoss(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        if (!checkService.checkRolePermission(id, storage_loss)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_loss_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = storageStockService.handleStorageStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addLossInfo(int id, int oid, String remark) {
        return addPurchaseInfo(id, oid, remark);
    }

    public RestResult delLossInfo(int id, int oid, int rid) {
        return delPurchaseInfo(id, oid, rid);
    }

    /**
     * desc: 仓储退货
     */
    public RestResult returnc(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 采购单未审核不能退货
        int rid = order.getOid();
        TPurchaseOrder purchaseOrder = purchaseOrderRepository.find(rid);
        if (null == purchaseOrder) {
            return RestResult.fail("未查询到采购单");
        }
        if (!purchaseOrder.getOtype().equals(PURCHASE_PURCHASE_ORDER.getValue())) {
            return RestResult.fail("进货单据类型异常");
        }
        if (null == purchaseOrder.getReview()) {
            return RestResult.fail("采购单未审核通过，不能进行退货");
        }
        if (!storagePurchaseRepository.checkByPid(rid)) {
            return RestResult.fail("采购商品未入库，请使用采购退货单");
        }

        order.setGid(purchaseOrder.getGid());
        order.setSid(purchaseOrder.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_return_apply, mp_storage_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createReturnComms(order, order.getOid(), types, commoditys, prices, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成退货单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 仓储退货修改
     */
    public RestResult setReturn(int id, int oid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_return_apply, mp_storage_return_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成退货单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createReturnComms(order, order.getOid(), types, commoditys, prices, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成退货订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
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
        storageAttachmentRepository.deleteByOid(oid);
        if (!storageCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
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
        int gid = group.getGid();

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出采购单
        // 注意: 仓储退货不修改进货单信息
        TPurchaseOrder purchase = purchaseOrderRepository.find(order.getOid());
        if (null == purchase) {
            return RestResult.fail("未查询到对应的进货单");
        }
        int unit = purchase.getUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("退货商品总量不能超出采购订单总量");
        }
        BigDecimal price = purchase.getPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("退货商品总价不能超出采购订单总价");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!storageReturnRepository.insert(oid, order.getOid())) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // 减少库存
        String msg = storageStockService.handleStorageStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 财务记录
        if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_RET, oid, order.getPrice())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = storageFareRepository.findByOid(oid);
        if (null != fares && !fares.isEmpty()) {
            for (TStorageFare fare : fares) {
                if (null == fare.getReview()) {
                    fare.setReview(id);
                    fare.setReviewTime(reviewTime);
                    if (!storageFareRepository.update(fare)) {
                        return RestResult.fail("更新运费信息失败");
                    }
                    if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_FARE2, oid, fare.getFare().negate())) {
                        return RestResult.fail("添加运费记录失败");
                    }
                }
            }
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeReturn(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
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

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_return_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!storageReturnRepository.delete(oid)) {
            return RestResult.fail("添加采购退货信息失败");
        }

        // 增加库存
        msg = storageStockService.handleStorageStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 财务记录
        BigDecimal money = storageCommodityRepository.count(oid);
        if (!financeService.insertRecord(id, gid, FINANCE_STORAGE_RET, oid, money.negate())) {
            return RestResult.fail("添加财务记录失败");
        }
        val fares = storageFareRepository.findByOid(oid);
        if (null != fares && !fares.isEmpty()) {
            for (TStorageFare fare : fares) {
                if (null != fare.getReview()) {
                    if (!financeService.insertRecord(id, gid, FINANCE_PURCHASE_FARE2, oid, fare.getFare())) {
                        return RestResult.fail("添加运费记录失败");
                    }
                }
            }
            if (!storageFareRepository.setReviewNull(oid)) {
                return RestResult.fail("更新运费信息失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult addReturnInfo(int id, int oid, BigDecimal fare, String remark) {
        return addDispatchInfo(id, oid, fare, remark);
    }

    public RestResult delReturnInfo(int id, int oid, int fid, int rid) {
        return delDispatchInfo(id, oid, fid, rid);
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

    private RestResult createPurchaseComms(TStorageOrder order, int pid, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TStorageCommodity> list) {
        // 生成入库单
        int size = commoditys.size();
        if (size != types.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val purchaseCommodities = purchaseCommodityRepository.find(pid);
        if (null == purchaseCommodities || purchaseCommodities.isEmpty()) {
            return RestResult.fail("未查询到采购商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TPurchaseCommodity pc : purchaseCommodities) {
                if (pc.getCtype() == ctype && pc.getCid() == cid) {
                    find = true;
                    if (weight > pc.getWeight()) {
                        return RestResult.fail("入库商品重量不能大于采购重量:" + ctype + ", 商品id:" + cid);
                    }
                    if (value > pc.getValue()) {
                        return RestResult.fail("入库商品件数不能大于采购件数:" + ctype + ", 商品id:" + cid);
                    }

                    TStorageCommodity c = new TStorageCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    if (weight == pc.getWeight()) {
                        c.setPrice(pc.getPrice());
                    } else {
                        c.setPrice(pc.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(pc.getWeight()), 2, RoundingMode.DOWN));
                    }
                    c.setWeight(weight);
                    c.setNorm(pc.getNorm());
                    c.setValue(value);
                    list.add(c);

                    total = total + weight;
                    price = price.add(c.getPrice());
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

    private RestResult createDispatchComms(TStorageOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TStorageCommodity> list) {
        // 生成调度单
        int size = commoditys.size();
        if (size != types.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int sid = order.getSid();
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            TStock stock = stockRepository.find(sid, ctype, cid);
            if (null == stock) {
                return RestResult.fail("未查询到库存类型:" + ctype + ",商品:" + cid);
            }
            if (weight > stock.getWeight()) {
                return RestResult.fail("库存商品重量不足:" + ctype + ",商品:" + cid);
            }
            if (value > stock.getValue()) {
                return RestResult.fail("库存商品件数不足:" + ctype + ",商品:" + cid);
            }

            TStorageCommodity c = new TStorageCommodity();
            c.setCtype(ctype);
            c.setCid(cid);
            if (weight == stock.getWeight()) {
                c.setPrice(stock.getPrice());
            } else {
                c.setPrice(stock.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(stock.getWeight()), 2, RoundingMode.DOWN));
            }
            c.setWeight(weight);
            c.setNorm(0);
            c.setValue(value);
            list.add(c);

            total = total + weight;
            price = price.add(c.getPrice());
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }

    private RestResult createPurchase2Comms(TStorageOrder order, int did, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TStorageCommodity> list) {
        // 生成入库单
        int size = commoditys.size();
        if (size != types.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val dispatchCommodities = storageCommodityRepository.find(did);
        if (null == dispatchCommodities || dispatchCommodities.isEmpty()) {
            return RestResult.fail("未查询到调度商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TStorageCommodity sc : dispatchCommodities) {
                if (sc.getCtype() == ctype && sc.getCid() == cid) {
                    find = true;
                    if (weight > sc.getWeight()) {
                        return RestResult.fail("入库商品重量不能大于调度重量:" + ctype + ", 商品id:" + cid);
                    }
                    if (value > sc.getValue()) {
                        return RestResult.fail("入库商品件数不能大于调度件数:" + ctype + ", 商品id:" + cid);
                    }

                    TStorageCommodity c = new TStorageCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    if (weight == sc.getWeight()) {
                        c.setPrice(sc.getPrice());
                    } else {
                        c.setPrice(sc.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(sc.getWeight()), 2, RoundingMode.DOWN));
                    }
                    c.setWeight(weight);
                    c.setNorm(sc.getNorm());
                    c.setValue(value);
                    list.add(c);

                    total = total + weight;
                    price = price.add(c.getPrice());
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

    private RestResult createAgreementComms(TStorageOrder order, int aid, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TStorageCommodity> list) {
        // 生成入库单
        int size = commoditys.size();
        if (size != types.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val agreementCommodities = agreementCommodityRepository.find(aid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到履约商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TAgreementCommodity ac : agreementCommodities) {
                if (ac.getCtype() == ctype && ac.getCid() == cid) {
                    find = true;
                    if (weight > ac.getWeight()) {
                        return RestResult.fail("入库商品数量不能大于履约数量:" + ctype + ", 商品id:" + cid);
                    }
                    if (value > ac.getValue()) {
                        return RestResult.fail("入库商品数量不能大于履约数量:" + ctype + ", 商品id:" + cid);
                    }

                    TStorageCommodity c = new TStorageCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    if (weight == ac.getWeight()) {
                        c.setPrice(ac.getPrice());
                    } else {
                        c.setPrice(ac.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(ac.getWeight()), 2, RoundingMode.DOWN));
                    }
                    c.setWeight(weight);
                    c.setNorm(ac.getNorm());
                    c.setValue(value);
                    list.add(c);

                    total = total + weight;
                    price = price.add(c.getPrice());
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

    private RestResult createReturnComms(TStorageOrder order, int pid, List<Integer> types, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<TStorageCommodity> list) {
        // 生成入库单
        int size = commoditys.size();
        if (size != types.size() || size != prices.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val purchaseCommodities = purchaseCommodityRepository.find(pid);
        if (null == purchaseCommodities || purchaseCommodities.isEmpty()) {
            return RestResult.fail("未查询到采购商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TPurchaseCommodity pc : purchaseCommodities) {
                if (pc.getCtype() == ctype && pc.getCid() == cid) {
                    find = true;
                    if (weight > pc.getWeight()) {
                        return RestResult.fail("入库商品重量不能大于采购重量:" + ctype + ", 商品id:" + cid);
                    }
                    if (value > pc.getValue()) {
                        return RestResult.fail("入库商品件数不能大于采购件数:" + ctype + ", 商品id:" + cid);
                    }

                    TStorageCommodity c = new TStorageCommodity();
                    c.setCtype(ctype);
                    c.setCid(cid);
                    c.setPrice(prices.get(i));
                    c.setWeight(weight);
                    c.setNorm(pc.getNorm());
                    c.setValue(value);
                    list.add(c);

                    total = total + weight;
                    price = price.add(c.getPrice());
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
