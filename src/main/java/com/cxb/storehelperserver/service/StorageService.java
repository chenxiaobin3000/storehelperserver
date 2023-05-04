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
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.*;
import static com.cxb.storehelperserver.util.TypeDefine.SaleType.SALE_CONST;
import static com.cxb.storehelperserver.util.TypeDefine.SaleType.SALE_ADD;

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
    private StockService stockService;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;

    @Resource
    private StorageAttachmentRepository storageAttachmentRepository;

    @Resource
    private StorageTypeRepository storageTypeRepository;

    @Resource
    private AgreementStorageRepository agreementStorageRepository;

    @Resource
    private AgreementCommodityRepository agreementCommodityRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private OfflineStorageRepository offlineStorageRepository;

    @Resource
    private OfflineCommodityRepository offlineCommodityRepository;

    @Resource
    private OfflineOrderRepository offlineOrderRepository;

    @Resource
    private ProductStorageRepository productStorageRepository;

    @Resource
    private ProductCommodityRepository productCommodityRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private PurchaseOrderRepository purchaseOrderRepository;

    @Resource
    private PurchaseCommodityRepository purchaseCommodityRepository;

    @Resource
    private PurchaseStorageRepository purchaseStorageRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult getStorageType(int id, int gid) {
        val data = new HashMap<String, Object>();
        data.put("list", storageTypeRepository.findByGroup(gid));
        return RestResult.ok(data);
    }

    /**
     * desc: 采购入库
     */
    public RestResult purchaseIn(int id, TStorageOrder order, int pid, int review, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 采购单未审核不能入库
        TPurchaseOrder purchase = purchaseOrderRepository.find(pid);
        if (null == purchase) {
            return RestResult.fail("未查询到采购单信息");
        }
        if (!purchase.getOtype().equals(PURCHASE_PURCHASE_ORDER.getValue())) {
            return RestResult.fail("采购单据类型异常");
        }
        if (null == purchase.getReview()) {
            return RestResult.fail("采购单未审核通过，不能进行入库");
        }

        order.setGid(purchase.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchaseComms(order, pid, commoditys, weights, values, comms);
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

        // 添加关联
        if (!purchaseStorageRepository.insert(pid, oid)) {
            return RestResult.fail("添加采购入库信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewPurchaseIn(id, oid);
        }
        return ret;
    }

    /**
     * desc: 采购入库修改
     */
    public RestResult setPurchaseIn(int id, int type, int oid, Date applyTime, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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
        if (!order.getOtype().equals(type)) {
            return RestResult.fail("修改订单类型错误");
        }

        // 采购单
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到采购单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchaseComms(order, pid, commoditys, weights, values, comms);
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

    public RestResult delPurchaseIn(int id, int oid) {
        // 删除关联
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到采购单信息");
        }
        if (!purchaseStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除采购入库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewPurchaseIn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 采购单
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到采购单信息");
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
        TPurchaseOrder purchase = purchaseOrderRepository.find(pid);
        if (null == purchase) {
            return RestResult.fail("未查询到对应的采购单");
        }
        int unit = purchase.getCurUnit() - order.getUnit();
        if (unit < 0) {
            log.info("---------"+ purchase.getCurUnit()+ "------"+ order.getUnit()+ "------"+ unit);
            return RestResult.fail("入库商品总量不能超出采购订单总量");
        }
        BigDecimal price = purchase.getCurPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("入库商品总价不能超出采购订单总价");
        }
        if (0 == unit) {
            purchase.setComplete(new Byte("1"));
        }
        purchase.setCurUnit(unit);
        purchase.setCurPrice(price);
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改采购单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 增加库存
        String msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokePurchaseIn(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 采购单
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到采购单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 还原扣除的采购单数量
        TPurchaseOrder purchase = purchaseOrderRepository.find(pid);
        if (null == purchase) {
            return RestResult.fail("未查询到对应的采购单");
        }
        purchase.setCurUnit(purchase.getCurUnit() + order.getUnit());
        purchase.setCurPrice(purchase.getCurPrice().add(order.getPrice()));
        purchase.setComplete(new Byte("0"));
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改采购单数据失败");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_purchase_in);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 采购出库
     */
    public RestResult purchaseOut(int id, TStorageOrder order, int pid, int review, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 采购单未审核不能退货
        TPurchaseOrder purchase = purchaseOrderRepository.find(pid);
        if (null == purchase) {
            return RestResult.fail("未查询到采购单信息");
        }
        if (!purchase.getOtype().equals(PURCHASE_RETURN_ORDER.getValue())) {
            return RestResult.fail("采购单据类型异常");
        }
        if (null == purchase.getReview()) {
            return RestResult.fail("采购单未审核通过，不能进行退货");
        }

        order.setGid(purchase.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchaseComms(order, pid, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成出库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加关联
        if (!purchaseStorageRepository.insert(pid, oid)) {
            return RestResult.fail("添加采购出库信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewPurchaseOut(id, oid);
        }
        return ret;
    }

    /**
     * desc: 采购出库修改
     */
    public RestResult setPurchaseOut(int id, int type, int oid, Date applyTime, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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
        if (!order.getOtype().equals(type)) {
            return RestResult.fail("修改订单类型错误");
        }

        // 退货单
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到退货单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_purchase_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createPurchaseComms(order, pid, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delPurchaseOut(int id, int oid) {
        // 删除关联
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到退货单信息");
        }
        if (!purchaseStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除退货出库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewPurchaseOut(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 退货单
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到退货单信息");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出采购单
        TPurchaseOrder purchase = purchaseOrderRepository.find(pid);
        if (null == purchase) {
            return RestResult.fail("未查询到对应的退货单");
        }
        int unit = purchase.getUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("出库商品总量不能超出退货订单总量");
        }
        BigDecimal price = purchase.getPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("出库商品总价不能超出退货订单总价");
        }
        if (0 == unit) {
            purchase.setComplete(new Byte("1"));
        }
        purchase.setCurUnit(unit);
        purchase.setCurPrice(price);
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改退货单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokePurchaseOut(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 退货单
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到退货单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 还原扣除的采购单数量
        TPurchaseOrder purchase = purchaseOrderRepository.find(pid);
        if (null == purchase) {
            return RestResult.fail("未查询到对应的退货单");
        }
        purchase.setCurUnit(purchase.getCurUnit() + order.getUnit());
        purchase.setCurPrice(purchase.getCurPrice().add(order.getPrice()));
        purchase.setComplete(new Byte("0"));
        if (!purchaseOrderRepository.update(purchase)) {
            return RestResult.fail("修改退货单数据失败");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_purchase_out);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产入库
     */
    public RestResult productIn(int id, TStorageOrder order, int pid, int review, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 生产单未审核不能入库
        TProductOrder product = productOrderRepository.find(pid);
        if (null == product) {
            return RestResult.fail("未查询到生产单信息");
        }
        if (!product.getOtype().equals(PRODUCT_COMPLETE_ORDER.getValue())) {
            return RestResult.fail("生产单据类型异常");
        }
        if (null == product.getReview()) {
            return RestResult.fail("生产单未审核通过，不能进行入库");
        }

        order.setGid(product.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_product_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createProductComms(order, pid, commoditys, weights, values, comms);
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

        // 添加关联
        if (!productStorageRepository.insert(pid, oid)) {
            return RestResult.fail("添加生产入库信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewProductIn(id, oid);
        }
        return ret;
    }

    /**
     * desc: 生产入库修改
     */
    public RestResult setProductIn(int id, int type, int oid, Date applyTime, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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
        if (!order.getOtype().equals(type)) {
            return RestResult.fail("修改订单类型错误");
        }

        // 生产单
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_product_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createProductComms(order, pid, commoditys, weights, values, comms);
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

    public RestResult delProductIn(int id, int oid) {
        // 删除关联
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }
        if (!productStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除生产入库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewProductIn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 生产单
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验入库总量不能超出生产单
        TProductOrder product = productOrderRepository.find(pid);
        if (null == product) {
            return RestResult.fail("未查询到对应的生产单");
        }
        int unit = product.getUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("入库商品总量不能超出生产订单总量");
        }
        BigDecimal price = product.getPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("入库商品总价不能超出生产订单总价");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 增加库存
        String msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeProductIn(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 生产单
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_product_in);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产出库
     */
    public RestResult productOut(int id, TStorageOrder order, int pid, int review, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 生产单未审核不能退货
        TProductOrder product = productOrderRepository.find(pid);
        if (null == product) {
            return RestResult.fail("未查询到生产单信息");
        }
        if (!product.getOtype().equals(PRODUCT_PROCESS_ORDER.getValue()) && !product.getOtype().equals(PRODUCT_LOSS_ORDER.getValue())) {
            return RestResult.fail("生产单据类型异常");
        }
        if (null == product.getReview()) {
            return RestResult.fail("生产单未审核通过，不能进行退货");
        }

        order.setGid(product.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_product_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createProductComms(order, pid, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成出库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加关联
        if (!productStorageRepository.insert(pid, oid)) {
            return RestResult.fail("添加生产出库信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewProductOut(id, oid);
        }
        return ret;
    }

    /**
     * desc: 生产出库修改
     */
    public RestResult setProductOut(int id, int type, int oid, Date applyTime, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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
        if (!order.getOtype().equals(type)) {
            return RestResult.fail("修改订单类型错误");
        }

        // 生产单
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_product_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createProductComms(order, pid, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delProductOut(int id, int oid) {
        // 删除关联
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }
        if (!productStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除生产出库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewProductOut(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 生产单
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出生产单
        TProductOrder product = productOrderRepository.find(pid);
        if (null == product) {
            return RestResult.fail("未查询到对应的生产单");
        }
        int unit = product.getUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("出库商品总量不能超出生产订单总量");
        }
        BigDecimal price = product.getPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("出库商品总价不能超出生产订单总价");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeProductOut(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 生产单
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_product_out);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 履约入库
     */
    public RestResult agreementIn(int id, TStorageOrder order, int pid, int review, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 履约单未审核不能入库
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到履约单信息");
        }
        if (!agreement.getOtype().equals(AGREEMENT_RETURN_ORDER.getValue())) {
            return RestResult.fail("履约单据类型异常");
        }
        if (null == agreement.getReview()) {
            return RestResult.fail("履约单未审核通过，不能进行入库");
        }

        order.setGid(agreement.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_agreement_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createAgreementComms(order, pid, commoditys, weights, values, comms);
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

        // 添加关联
        if (!agreementStorageRepository.insert(pid, oid)) {
            return RestResult.fail("添加履约入库信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewAgreementIn(id, oid);
        }
        return ret;
    }

    /**
     * desc: 履约入库修改
     */
    public RestResult setAgreementIn(int id, int type, int oid, Date applyTime, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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
        if (!order.getOtype().equals(type)) {
            return RestResult.fail("修改订单类型错误");
        }

        // 履约单
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到履约单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_agreement_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createAgreementComms(order, pid, commoditys, weights, values, comms);
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

    public RestResult delAgreementIn(int id, int oid) {
        // 删除关联
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到履约单信息");
        }
        if (!agreementStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除履约入库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewAgreementIn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 履约单
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到履约单信息");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验入库总量不能超出履约单
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到对应的履约单");
        }
        int value = agreement.getCurValue() - order.getUnit();
        if (value < 0) {
            return RestResult.fail("入库商品总量不能超出履约订单总量");
        }
        BigDecimal price = agreement.getCurPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("入库商品总价不能超出履约订单总价");
        }
        if (0 == value) {
            agreement.setComplete(new Byte("1"));
        }
        agreement.setCurValue(value);
        agreement.setCurPrice(price);
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

        // 增加库存
        String msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeAgreementIn(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 履约单
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到履约单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 还原扣除的履约单数量
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到对应的履约单");
        }
        agreement.setCurValue(agreement.getCurValue() + order.getUnit());
        agreement.setCurPrice(agreement.getCurPrice().add(order.getPrice()));
        agreement.setComplete(new Byte("0"));
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改履约单数据失败");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_agreement_in);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 履约出库
     */
    public RestResult agreementOut(int id, TStorageOrder order, int pid, int review, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 履约单未审核不能退货
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到履约单信息");
        }
        if (!agreement.getOtype().equals(AGREEMENT_SHIPPED_ORDER.getValue())) {
            return RestResult.fail("履约单据类型异常");
        }
        if (null == agreement.getReview()) {
            return RestResult.fail("履约单未审核通过，不能进行出库");
        }

        order.setGid(agreement.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_agreement_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createAgreementComms(order, pid, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成出库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加关联
        if (!agreementStorageRepository.insert(pid, oid)) {
            return RestResult.fail("添加履约出库信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewAgreementOut(id, oid);
        }
        return ret;
    }

    /**
     * desc: 履约出库修改
     */
    public RestResult setAgreementOut(int id, int type, int oid, Date applyTime, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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
        if (!order.getOtype().equals(type)) {
            return RestResult.fail("修改订单类型错误");
        }

        // 履约单
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到履约单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_agreement_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createAgreementComms(order, pid, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delAgreementOut(int id, int oid) {
        // 删除关联
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到履约单信息");
        }
        if (!agreementStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除履约出库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewAgreementOut(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 履约单
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到履约单信息");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出采购单
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到对应的履约单");
        }
        int value = agreement.getValue() - order.getUnit();
        if (value < 0) {
            return RestResult.fail("出库商品总量不能超出履约订单总量");
        }
        BigDecimal price = agreement.getPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("出库商品总价不能超出履约订单总价");
        }
        if (0 == value) {
            agreement.setComplete(new Byte("1"));
        }
        agreement.setCurValue(value);
        agreement.setCurPrice(price);
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

        // 减少库存
        String msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeAgreementOut(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 履约单
        int pid = getAgreementId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到履约单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 还原扣除的履约单数量
        TAgreementOrder agreement = agreementOrderRepository.find(pid);
        if (null == agreement) {
            return RestResult.fail("未查询到对应的履约单");
        }
        agreement.setCurValue(agreement.getCurValue() + order.getUnit());
        agreement.setCurPrice(agreement.getCurPrice().add(order.getPrice()));
        agreement.setComplete(new Byte("0"));
        if (!agreementOrderRepository.update(agreement)) {
            return RestResult.fail("修改履约单数据失败");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_agreement_out);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 线下销售入库
     */
    public RestResult offlineIn(int id, TStorageOrder order, int pid, int review, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 退货单未审核不能入库
        TOfflineOrder offline = offlineOrderRepository.find(pid);
        if (null == offline) {
            return RestResult.fail("未查询到退货单信息");
        }
        if (!offline.getOtype().equals(OFFLINE_RETURN_ORDER.getValue())) {
            return RestResult.fail("退货单据类型异常");
        }
        if (null == offline.getReview()) {
            return RestResult.fail("退货单未审核通过，不能进行入库");
        }

        order.setGid(offline.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_offline_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createOfflineComms(order, pid, commoditys, weights, values, comms);
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

        // 添加关联
        if (!offlineStorageRepository.insert(pid, oid)) {
            return RestResult.fail("添加退货入库信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewOfflineIn(id, oid);
        }
        return ret;
    }

    /**
     * desc: 线下销售入库修改
     */
    public RestResult setOfflineIn(int id, int type, int oid, Date applyTime, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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
        if (!order.getOtype().equals(type)) {
            return RestResult.fail("修改订单类型错误");
        }

        // 退货单
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到退货单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_offline_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createOfflineComms(order, pid, commoditys, weights, values, comms);
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

    public RestResult delOfflineIn(int id, int oid) {
        // 删除关联
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到退货单信息");
        }
        if (!offlineStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除退货入库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewOfflineIn(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 退货单
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到退货单信息");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验入库总量不能超出退货单
        TOfflineOrder offline = offlineOrderRepository.find(pid);
        if (null == offline) {
            return RestResult.fail("未查询到对应的退货单");
        }
        int value = offline.getCurValue() - order.getUnit();
        if (value < 0) {
            return RestResult.fail("入库商品总量不能超出退货订单总量");
        }
        BigDecimal price = offline.getCurPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("入库商品总价不能超出退货订单总价");
        }
        if (0 == value) {
            offline.setComplete(new Byte("1"));
        }
        offline.setCurValue(value);
        offline.setCurPrice(price);
        if (!offlineOrderRepository.update(offline)) {
            return RestResult.fail("修改退货单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 增加库存
        String msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeOfflineIn(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 退货单
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到退货单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 还原扣除的退货单数量
        TOfflineOrder offline = offlineOrderRepository.find(pid);
        if (null == offline) {
            return RestResult.fail("未查询到对应的退货单");
        }
        offline.setCurValue(offline.getCurValue() + order.getUnit());
        offline.setCurPrice(offline.getCurPrice().add(order.getPrice()));
        offline.setComplete(new Byte("0"));
        if (!offlineOrderRepository.update(offline)) {
            return RestResult.fail("修改退货单数据失败");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_offline_in);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 线下销售出库
     */
    public RestResult offlineOut(int id, TStorageOrder order, int pid, int review, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 销售单未审核不能退货
        TOfflineOrder offline = offlineOrderRepository.find(pid);
        if (null == offline) {
            return RestResult.fail("未查询到销售单信息");
        }
        if (!offline.getOtype().equals(OFFLINE_OFFLINE_ORDER.getValue())) {
            return RestResult.fail("销售单据类型异常");
        }
        if (null == offline.getReview()) {
            return RestResult.fail("销售单未审核通过，不能进行出库");
        }

        order.setGid(offline.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_offline_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createOfflineComms(order, pid, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成出库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加关联
        if (!offlineStorageRepository.insert(pid, oid)) {
            return RestResult.fail("添加销售出库信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewOfflineOut(id, oid);
        }
        return ret;
    }

    /**
     * desc: 线下销售出库修改
     */
    public RestResult setOfflineOut(int id, int type, int oid, Date applyTime, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
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
        if (!order.getOtype().equals(type)) {
            return RestResult.fail("修改订单类型错误");
        }

        // 销售单
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到销售单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_offline_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createOfflineComms(order, pid, commoditys, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delOfflineOut(int id, int oid) {
        // 删除关联
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到销售单信息");
        }
        if (!offlineStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除销售出库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewOfflineOut(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 销售单
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到销售单信息");
        }

        // 校验审核人员信息
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验退货订单总价格和总量不能超出采购单
        TOfflineOrder offline = offlineOrderRepository.find(pid);
        if (null == offline) {
            return RestResult.fail("未查询到对应的销售单");
        }
        int value = offline.getValue() - order.getUnit();
        if (value < 0) {
            return RestResult.fail("出库商品总量不能超出销售订单总量");
        }
        BigDecimal price = offline.getPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("出库商品总价不能超出销售订单总价");
        }
        if (0 == value) {
            offline.setComplete(new Byte("1"));
        }
        offline.setCurValue(value);
        offline.setCurPrice(price);
        if (!offlineOrderRepository.update(offline)) {
            return RestResult.fail("修改销售单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeOfflineOut(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }

        // 销售单
        int pid = getOfflineId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到销售单信息");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 还原扣除的销售单数量
        TOfflineOrder offline = offlineOrderRepository.find(pid);
        if (null == offline) {
            return RestResult.fail("未查询到对应的销售单");
        }
        offline.setCurValue(offline.getCurValue() + order.getUnit());
        offline.setCurPrice(offline.getCurPrice().add(order.getPrice()));
        offline.setComplete(new Byte("0"));
        if (!offlineOrderRepository.update(offline)) {
            return RestResult.fail("修改销售单数据失败");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_offline_out);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 调度入库
     */
    public RestResult dispatchIn(int id, TStorageOrder order, int review, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_dispatch_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageInComms(order, commoditys, prices, weights, norms, values, comms);
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

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewDispatchIn(id, oid);
        }
        return ret;
    }

    /**
     * desc: 调度入库修改
     */
    public RestResult setDispatchIn(int id, int oid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
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
        RestResult ret = check(id, order, mp_storage_dispatch_in, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成入库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageInComms(order, commoditys, prices, weights, norms, values, comms);
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

    public RestResult delDispatchIn(int id, int oid) {
        // 删除关联
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到采购单信息");
        }
        if (!purchaseStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除采购入库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewDispatchIn(int id, int oid) {
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

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 增加库存
        String msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeDispatchIn(int id, int oid) {
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

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_dispatch_in);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 调度出库
     */
    public RestResult dispatchOut(int id, TStorageOrder order, int review, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_dispatch_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageOutComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成出库单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        int oid = order.getId();
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewDispatchOut(id, oid);
        }
        return ret;
    }

    /**
     * desc: 调度出库修改
     */
    public RestResult setDispatchOut(int id, int oid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
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
        RestResult ret = check(id, order, mp_storage_dispatch_out, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成出库单
        val comms = new ArrayList<TStorageCommodity>();
        ret = createStorageOutComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("生成出库订单失败");
        }
        String msg = storageOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delDispatchOut(int id, int oid) {
        // 删除关联
        int pid = getPurchaseId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到退货单信息");
        }
        if (!purchaseStorageRepository.delete(oid, pid)) {
            return RestResult.fail("删除退货出库信息失败");
        }
        return delOrder(id, oid);
    }

    public RestResult reviewDispatchOut(int id, int oid) {
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

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = stockService.handleStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeDispatchOut(int id, int oid) {
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

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_dispatch_out);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = stockService.handleStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 仓储损耗
     */
    public RestResult loss(int id, TStorageOrder order, int review, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_storage_loss, reviews);
        if (null != ret) {
            return ret;
        }

        // 校验损耗类型
        val losses = storageTypeRepository.findByGroup(order.getGid());
        if (null == losses) {
            return RestResult.fail("未查询到损耗类型信息");
        }
        int tid = order.getTid();
        boolean find = false;
        boolean add = false;
        for (TStorageType loss : losses) {
            if (loss.getId().equals(tid)) {
                add = loss.getIsAdd().equals(1);
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("未查询到对应的损耗类型");
        }

        // 生成损耗单
        val comms = new ArrayList<TStorageCommodity>();
        if (add) {
            ret = createStorageInComms(order, commoditys, prices, weights, norms, values, comms);
        } else {
            ret = createStorageOutComms(order, commoditys, prices, weights, norms, values, comms);
        }
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

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            return reviewLoss(id, oid);
        }
        return ret;
    }

    /**
     * desc: 仓储损耗修改
     */
    public RestResult setLoss(int id, int oid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
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
        RestResult ret = check(id, order, mp_storage_loss, reviews);
        if (null != ret) {
            return ret;
        }

        // 校验损耗类型
        val losses = storageTypeRepository.findByGroup(order.getGid());
        if (null == losses) {
            return RestResult.fail("未查询到损耗类型信息");
        }
        int tid = order.getTid();
        boolean find = false;
        boolean add = false;
        for (TStorageType loss : losses) {
            if (loss.getId().equals(tid)) {
                add = loss.getIsAdd().equals(1);
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("未查询到对应的损耗类型");
        }

        // 生成损耗单
        val comms = new ArrayList<TStorageCommodity>();
        if (add) {
            ret = createStorageInComms(order, commoditys, prices, weights, norms, values, comms);
        } else {
            ret = createStorageOutComms(order, commoditys, prices, weights, norms, values, comms);
        }
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
        return delOrder(id, oid);
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

        // 损耗类型
        val losses = storageTypeRepository.findByGroup(order.getGid());
        if (null == losses) {
            return RestResult.fail("未查询到损耗类型信息");
        }
        int tid = order.getTid();
        boolean find = false;
        int add = 0;
        for (TStorageType loss : losses) {
            if (loss.getId().equals(tid)) {
                add = loss.getIsAdd();
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("未查询到对应的损耗类型");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!storageOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 操作库存
        if (SALE_CONST.getValue() != add) {
            String msg = stockService.handleStock(order, SALE_ADD.getValue() == add);
            if (null != msg) {
                return RestResult.fail(msg);
            }
        }
        return reviewService.review(order.getApply(), id, gid, order.getOtype(), oid, order.getBatch(), order.getApplyTime());
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

        // 损耗类型
        val losses = storageTypeRepository.findByGroup(gid);
        if (null == losses) {
            return RestResult.fail("未查询到损耗类型信息");
        }
        int tid = order.getTid();
        boolean find = false;
        int add = 0;
        for (TStorageType loss : losses) {
            if (loss.getId().equals(tid)) {
                add = loss.getIsAdd();
                find = true;
                break;
            }
        }
        if (!find) {
            return RestResult.fail("未查询到对应的损耗类型");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_storage_loss);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!storageOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 操作库存
        if (SALE_CONST.getValue() != add) {
            msg = stockService.handleStock(order, SALE_ADD.getValue() != add);
            if (null != msg) {
                return RestResult.fail(msg);
            }
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TStorageOrder order, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(gid, reviewPerm, reviews);
    }

    private RestResult createPurchaseComms(TStorageOrder order, int pid, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TStorageCommodity> list) {
        // 生成出库单
        int size = commoditys.size();
        if (size != weights.size() || size != values.size()) {
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
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TPurchaseCommodity pc : purchaseCommodities) {
                if (pc.getCid() == cid) {
                    find = true;
                    if (weight > pc.getWeight()) {
                        return RestResult.fail("入库商品重量不能大于采购重量, 商品id:" + cid);
                    }
                    if (value > pc.getValue()) {
                        return RestResult.fail("入库商品件数不能大于采购件数, 商品id:" + cid);
                    }

                    TStorageCommodity c = new TStorageCommodity();
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
                return RestResult.fail("未查询到商品id:" + cid);
            }
        }
        order.setUnit(total);
        order.setPrice(price);
        return null;
    }

    private RestResult createProductComms(TStorageOrder order, int pid, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TStorageCommodity> list) {
        // 生成生产单
        int size = commoditys.size();
        if (size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val productCommodities = productCommodityRepository.find(pid);
        if (null == productCommodities || productCommodities.isEmpty()) {
            return RestResult.fail("未查询到采购商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TProductCommodity pc : productCommodities) {
                if (pc.getCid() == cid) {
                    find = true;
                    if (weight > pc.getWeight()) {
                        return RestResult.fail("入库商品重量不能大于采购重量, 商品id:" + cid);
                    }
                    if (value > pc.getValue()) {
                        return RestResult.fail("入库商品件数不能大于采购件数, 商品id:" + cid);
                    }

                    TStorageCommodity c = new TStorageCommodity();
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
                return RestResult.fail("未查询到商品id:" + cid);
            }
        }
        order.setUnit(total);
        order.setPrice(price);
        return null;
    }

    private RestResult createAgreementComms(TStorageOrder order, int pid, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TStorageCommodity> list) {
        // 生成生产单
        int size = commoditys.size();
        if (size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val agreementCommodities = agreementCommodityRepository.find(pid);
        if (null == agreementCommodities || agreementCommodities.isEmpty()) {
            return RestResult.fail("未查询到采购商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TAgreementCommodity pc : agreementCommodities) {
                if (pc.getCid() == cid) {
                    find = true;
                    if (weight > pc.getWeight()) {
                        return RestResult.fail("入库商品重量不能大于采购重量, 商品id:" + cid);
                    }
                    if (value > pc.getValue()) {
                        return RestResult.fail("入库商品件数不能大于采购件数, 商品id:" + cid);
                    }

                    TStorageCommodity c = new TStorageCommodity();
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
                return RestResult.fail("未查询到商品id:" + cid);
            }
        }
        order.setUnit(total);
        order.setPrice(price);
        return null;
    }

    private RestResult createOfflineComms(TStorageOrder order, int pid, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TStorageCommodity> list) {
        // 生成生产单
        int size = commoditys.size();
        if (size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val offlineCommodities = offlineCommodityRepository.find(pid);
        if (null == offlineCommodities || offlineCommodities.isEmpty()) {
            return RestResult.fail("未查询到采购商品信息");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            boolean find = false;
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            for (TOfflineCommodity pc : offlineCommodities) {
                if (pc.getCid() == cid) {
                    find = true;
                    if (weight > pc.getWeight()) {
                        return RestResult.fail("入库商品重量不能大于采购重量, 商品id:" + cid);
                    }
                    if (value > pc.getValue()) {
                        return RestResult.fail("入库商品件数不能大于采购件数, 商品id:" + cid);
                    }

                    TStorageCommodity c = new TStorageCommodity();
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
                return RestResult.fail("未查询到商品id:" + cid);
            }
        }
        order.setUnit(total);
        order.setPrice(price);
        return null;
    }

    private RestResult createStorageInComms(TStorageOrder order, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<TStorageCommodity> list) {
        // 生成调度单
        int size = commoditys.size();
        if (size != prices.size() || size != weights.size() || size != norms.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int sid = order.getSid();
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            TStorageCommodity c = new TStorageCommodity();
            c.setCid(commoditys.get(i));
            c.setPrice(prices.get(i));
            c.setWeight(weights.get(i));
            c.setNorm(norms.get(i));
            c.setValue(values.get(i));
            list.add(c);

            total = total + c.getWeight();
            price = price.add(c.getPrice());
        }
        order.setUnit(total);
        order.setPrice(price);
        return null;
    }

    private RestResult createStorageOutComms(TStorageOrder order, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<TStorageCommodity> list) {
        // 生成调度单
        int size = commoditys.size();
        if (size != prices.size() || size != weights.size() || size != norms.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int sid = order.getSid();
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            TStockDay stock = stockService.getStockCommodity(order.getGid(), sid, cid);
            if (null == stock) {
                return RestResult.fail("未查询到库存商品:" + cid);
            }
            if (weight > stock.getWeight()) {
                return RestResult.fail("库存商品重量商品:" + cid);
            }
            if (value > stock.getValue()) {
                return RestResult.fail("库存商品件数商品:" + cid);
            }

            TStorageCommodity c = new TStorageCommodity();
            c.setCid(cid);
            c.setPrice(prices.get(i));
            c.setWeight(weight);
            c.setNorm(norms.get(i));
            c.setValue(value);
            list.add(c);

            total = total + weight;
            price = price.add(c.getPrice());
        }
        order.setUnit(total);
        order.setPrice(price);
        return null;
    }

    // 获取入库对应的采购单
    private int getPurchaseId(int id) {
        TPurchaseStorage ret = purchaseStorageRepository.findBySid(id);
        if (null != ret) {
            return ret.getOid();
        }
        return 0;
    }

    // 获取入库对应的生产单
    private int getProductId(int id) {
        TProductStorage ret = productStorageRepository.findBySid(id);
        if (null != ret) {
            return ret.getOid();
        }
        return 0;
    }

    // 获取入库对应的履约单
    private int getAgreementId(int id) {
        TAgreementStorage ret = agreementStorageRepository.findBySid(id);
        if (null != ret) {
            return ret.getOid();
        }
        return 0;
    }

    // 获取入库对应的线下销售单
    private int getOfflineId(int id) {
        TOfflineStorage ret = offlineStorageRepository.findBySid(id);
        if (null != ret) {
            return ret.getOid();
        }
        return 0;
    }

    public RestResult delOrder(int id, int oid) {
        TStorageOrder order = storageOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
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
}
