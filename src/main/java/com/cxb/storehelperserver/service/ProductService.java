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
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.*;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.PRODUCT_PROCESS_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.STORAGE_PRODUCT_IN_ORDER;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.STORAGE_PRODUCT_OUT_ORDER;

/**
 * desc: 生产业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductService {
    @Resource
    private CheckService checkService;

    @Resource
    private ProductOrderService productOrderService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private StorageService storageService;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductCommodityRepository productCommodityRepository;

    @Resource
    private ProductAttachmentRepository productAttachmentRepository;

    @Resource
    private ProductCompleteRepository productCompleteRepository;

    @Resource
    private ProductRemarkRepository productRemarkRepository;

    @Resource
    private ProductStorageRepository productStorageRepository;

    @Resource
    private CommodityOriginalRepository commodityOriginalRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 生产出库
     */
    public RestResult process(int id, TProductOrder order, int sid, int review, int storage, List<Integer> commoditys, List<BigDecimal> prices,
                              List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_process, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成生产单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProcessComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成生产单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!productOrderRepository.insert(order)) {
            return RestResult.fail("生成生产订单失败");
        }
        int oid = order.getId();
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            ret = reviewProcess(id, oid);
            if (RestResult.isOk(ret) && storage > 0) {
                // 一键出库
                if (sid <= 0) {
                    return RestResult.fail("未指定仓库，一键出库失败");
                }
                TStorageOrder storageOrder = new TStorageOrder();
                storageOrder.setOtype(STORAGE_PRODUCT_OUT_ORDER.getValue());
                storageOrder.setSid(sid);
                storageOrder.setTid(0);
                storageOrder.setApply(order.getApply());
                storageOrder.setApplyTime(order.getApplyTime());
                return storageService.productOut(id, storageOrder, oid, review, commoditys, weights, values, attrs);
            }
        }
        return ret;
    }

    /**
     * desc: 生产出库修改
     */
    public RestResult setProcess(int id, int oid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TProductOrder order = productOrderRepository.find(oid);
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
        RestResult ret = check(id, order, mp_product_process, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成生产单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProcessComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("生成生产订单失败");
        }
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delProcess(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }

        // 已经审核的订单不能删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
        }

        // 删除商品附件数据
        productAttachmentRepository.deleteByOid(oid);
        if (!productCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!productOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewProcess(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }
        return reviewService.review(order.getApply(), id, group.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeProcess(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }
        // 已结算的不能撤销
        if (order.getUnit() > order.getCurUnit()) {
            return RestResult.fail("已结算的订单不能撤销");
        }
        // 存在出库单就不能撤销
        if (null != productStorageRepository.find(oid)) {
            return RestResult.fail("已出库的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_product_process);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产结算
     */
    public RestResult complete(int id, TProductOrder order, int rid, int sid, int review, int storage, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 生产单未审核不能结算
        TProductOrder process = productOrderRepository.find(rid);
        if (null == process) {
            return RestResult.fail("未查询到生产单");
        }
        if (!process.getOtype().equals(PRODUCT_PROCESS_ORDER.getValue())) {
            return RestResult.fail("生产单据类型异常");
        }
        if (null == process.getReview()) {
            return RestResult.fail("生产单未审核通过，不能进行结算");
        }

        order.setGid(process.getGid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_complete, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成结算单
        val comms = new ArrayList<TProductCommodity>();
        ret = createCompleteComms(order, rid, commoditys, prices, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成结算单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!productOrderRepository.insert(order)) {
            return RestResult.fail("生成结算订单失败");
        }
        int oid = order.getId();
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 添加关联
        if (!productCompleteRepository.insert(oid, rid)) {
            return RestResult.fail("添加生产结算信息失败");
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            ret = reviewComplete(id, oid);
            if (RestResult.isOk(ret) && storage > 0) {
                // 一键入库
                if (sid <= 0) {
                    return RestResult.fail("未指定仓库，一键入库失败");
                }
                TStorageOrder storageOrder = new TStorageOrder();
                storageOrder.setOtype(STORAGE_PRODUCT_IN_ORDER.getValue());
                storageOrder.setSid(sid);
                storageOrder.setTid(0);
                storageOrder.setApply(order.getApply());
                storageOrder.setApplyTime(order.getApplyTime());
                return storageService.purchaseIn(id, storageOrder, oid, review, commoditys, weights, values, attrs);
            }
        }
        return ret;
    }

    /**
     * desc: 原料结算修改
     */
    public RestResult setComplete(int id, int oid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要修改的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        // 生产单
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_complete, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成结算单
        val comms = new ArrayList<TProductCommodity>();
        ret = createCompleteComms(order, pid, commoditys, prices, weights, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("生成结算订单失败");
        }
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delComplete(int id, int oid) {
        // 删除关联
        int pid = getProductId(oid);
        if (0 == pid) {
            return RestResult.fail("未查询到生产单信息");
        }
        if (!productCompleteRepository.delete(oid, pid)) {
            return RestResult.fail("删除生产结算信息失败");
        }
        return delProcess(id, oid);
    }

    public RestResult reviewComplete(int id, int oid) {
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
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验结算订单总价格和总量不能超出生产单
        TProductOrder product = productOrderRepository.find(pid);
        if (null == product) {
            return RestResult.fail("未查询到对应的生产单");
        }
        int unit = product.getCurUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("结算商品总量不能超出生产订单总量");
        }
        BigDecimal price = product.getCurPrice().subtract(order.getPrice());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RestResult.fail("结算商品总价不能超出生产订单总价");
        }
        if (0 == unit) {
            product.setComplete(new Byte("1"));
        }
        product.setCurUnit(unit);
        product.setCurPrice(price);
        if (!productOrderRepository.update(product)) {
            return RestResult.fail("修改生产单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        order.setComplete(new Byte("1"));
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeComplete(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }
        // 存在入库单就不能改
        if (null != productStorageRepository.find(oid)) {
            return RestResult.fail("已入库的订单不能撤销");
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

        // 还原扣除的生产单数量
        TProductOrder product = productOrderRepository.find(pid);
        if (null == product) {
            return RestResult.fail("未查询到对应的生产单");
        }
        product.setCurUnit(product.getCurUnit() + order.getUnit());
        product.setCurPrice(product.getCurPrice().add(order.getPrice()));
        product.setComplete(new Byte("0"));
        if (!productOrderRepository.update(product)) {
            return RestResult.fail("修改生产单数据失败");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_product_complete);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产损耗
     */
    public RestResult loss(int id, TProductOrder order, int sid, int review, int storage, List<Integer> commoditys,
                           List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_loss, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProcessComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!productOrderRepository.insert(order)) {
            return RestResult.fail("生成损耗订单失败");
        }
        int oid = order.getId();
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 一键审核
        ret = reviewService.apply(id, order.getGid(), order.getOtype(), oid, batch, reviews);
        if (RestResult.isOk(ret) && review > 0) {
            ret = reviewLoss(id, oid);
            if (RestResult.isOk(ret) && storage > 0) {
                // 一键出库
                if (sid <= 0) {
                    return RestResult.fail("未指定仓库，一键出库失败");
                }
                TStorageOrder storageOrder = new TStorageOrder();
                storageOrder.setOtype(STORAGE_PRODUCT_OUT_ORDER.getValue());
                storageOrder.setSid(sid);
                storageOrder.setTid(0);
                storageOrder.setApply(order.getApply());
                storageOrder.setApplyTime(order.getApplyTime());
                return storageService.productOut(id, storageOrder, oid, review, commoditys, weights, values, attrs);
            }
        }
        return ret;
    }

    /**
     * desc: 生产损耗修改
     */
    public RestResult setLoss(int id, int oid, Date applyTime, List<Integer> commoditys, List<BigDecimal> prices, List<Integer> weights, List<String> norms, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TProductOrder order = productOrderRepository.find(oid);
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
        RestResult ret = check(id, order, mp_product_loss, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProcessComms(order, commoditys, prices, weights, norms, values, comms);
        if (null != ret) {
            return ret;
        }
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("生成损耗订单失败");
        }
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delLoss(int id, int oid) {
        return delProcess(id, oid);
    }

    public RestResult reviewLoss(int id, int oid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        // 校验审核人员信息
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeLoss(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要撤销的订单");
        }
        if (null == order.getReview()) {
            return RestResult.fail("未审核的订单不能撤销");
        }
        // 存在出库单就不能改
        if (null != productStorageRepository.find(oid)) {
            return RestResult.fail("已出库的订单不能撤销");
        }

        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        RestResult ret = reviewService.revoke(id, gid, order.getOtype(), oid, order.getBatch(), order.getApply(), mp_product_loss);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TProductOrder order, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(gid, reviewPerm, reviews);
    }

    private RestResult createProcessComms(TProductOrder order, List<Integer> commoditys, List<BigDecimal> prices,
                                          List<Integer> weights, List<String> norms, List<Integer> values, List<TProductCommodity> list) {
        // 生成采购单
        int size = commoditys.size();
        if (size != prices.size() || size != weights.size() || size != norms.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            TProductCommodity c = new TProductCommodity();
            c.setCid(commoditys.get(i));
            c.setPrice(prices.get(i));
            c.setWeight(weights.get(i));
            c.setNorm(norms.get(i));
            c.setValue(values.get(i));
            list.add(c);

            total = total + c.getWeight();
            price = price.add(prices.get(i));
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }

    private RestResult createCompleteComms(TProductOrder order, int rid, List<Integer> commoditys, List<BigDecimal> prices,
                                           List<Integer> weights, List<Integer> values, List<TProductCommodity> list) {
        // 生成结算单
        int size = commoditys.size();
        if (size != prices.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val productCommodities = productCommodityRepository.find(rid);
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
                        return RestResult.fail("结算商品重量不能大于采购重量, 商品id:" + cid);
                    }
                    if (value > pc.getValue()) {
                        return RestResult.fail("结算商品件数不能大于采购件数, 商品id:" + cid);
                    }

                    TProductCommodity c = new TProductCommodity();
                    c.setCid(cid);
                    c.setPrice(prices.get(i));
                    c.setWeight(weight);
                    c.setNorm(pc.getNorm());
                    c.setValue(value);
                    list.add(c);

                    total = total + c.getWeight();
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
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }

    // 获取结算对应的生产单
    private int getProductId(int id) {
        TProductComplete ret = productCompleteRepository.find(id);
        if (null != ret) {
            return ret.getPid();
        }
        return 0;
    }
}
