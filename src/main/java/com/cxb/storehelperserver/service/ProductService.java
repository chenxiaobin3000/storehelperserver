package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
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
    private StorageStockService storageStockService;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductCommodityRepository productCommodityRepository;

    @Resource
    private ProductAttachmentRepository productAttachmentRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private HalfgoodRepository halfgoodRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 生产开始
     */
    public RestResult process(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_process_apply, mp_product_process_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成生产单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProductComms(order, types, commoditys, values, comms);
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
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 生产开始修改
     */
    public RestResult setProcess(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_process_apply, mp_product_process_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        int oid = order.getId();
        TProductOrder productOrder = productOrderRepository.find(oid);
        if (null == productOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != productOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!productOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(order.getOtype(), oid, order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成生产单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProductComms(order, types, commoditys, values, comms);
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

        // 校验是否订单提交人，已经审核的订单，必须由审核人删除
        Integer review = order.getReview();
        if (null != review) {
            return RestResult.fail("已审核的订单不能删除");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("订单必须由申请人删除");
        }

        // 删除商品附件数据
        if (!productCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!productAttachmentRepository.deleteByOid(oid)) {
            return RestResult.fail("删除关联商品附件失败");
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

        // TODO 校验所有入库单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 减少库存
        String msg = storageStockService.addStock(id, false, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid, 0);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeProcess(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, product_process)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_product_process_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = storageStockService.addStock(id, true, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid, 0);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产完成
     */
    public RestResult complete(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_complete_apply, mp_product_complete_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成完成单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProductComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成完成单批号
        String batch = dateUtil.createBatch(order.getOtype());
        order.setBatch(batch);
        if (!productOrderRepository.insert(order)) {
            return RestResult.fail("生成进货订单失败");
        }
        int oid = order.getId();
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 生产完成修改
     */
    public RestResult setComplete(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_complete_apply, mp_product_complete_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        int oid = order.getId();
        TProductOrder productOrder = productOrderRepository.find(order.getId());
        if (null == productOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != productOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!productOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(order.getOtype(), oid, order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成进货单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProductComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        if (!productOrderRepository.update(order)) {
            return RestResult.fail("生成完成订单失败");
        }
        String msg = productOrderService.update(oid, comms, attrs);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult delComplete(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!productCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!productAttachmentRepository.deleteByOid(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!productOrderRepository.delete(oid)) {
            return RestResult.fail("删除订单失败");
        }
        return reviewService.delete(review, order.getOtype(), oid);
    }

    public RestResult reviewComplete(int id, int oid) {
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

        // TODO 校验所有入库单中的每一个商品总数，不能大于采购单中商品数量，申请时只校验单个单据，这里校验所有

        // 添加审核信息
        order.setReview(id);
        order.setReviewTime(new Date());
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 增加库存
        String msg = storageStockService.addStock(id, true, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid, 0);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeComplete(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, product_complete)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_product_complete_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        msg = storageStockService.addStock(id, false, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid, 0);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产损耗
     */
    public RestResult loss(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_loss_apply, mp_product_loss_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProductComms(order, types, commoditys, values, comms);
        if (null != ret) {
            return ret;
        }

        // 生成入库单批号
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
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 仓储损耗修改
     */
    public RestResult setLoss(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_loss_apply, mp_product_loss_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 已经审核的订单不能修改
        int oid = order.getId();
        TProductOrder productOrder = productOrderRepository.find(oid);
        if (null == productOrder) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != productOrder.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }

        // 更新仓库信息
        if (!productOrder.getSid().equals(order.getSid())) {
            ret = reviewService.update(order.getOtype(), oid, order.getSid());
            if (null != ret) {
                return ret;
            }
        }

        // 生成入库单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProductComms(order, types, commoditys, values, comms);
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
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!productCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
        }
        if (!productAttachmentRepository.deleteByOid(oid)) {
            return RestResult.fail("删除关联商品附件失败");
        }
        if (!productOrderRepository.delete(oid)) {
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
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 增加库存
        String msg = storageStockService.addStock(id, true, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid, 0);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, order.getGid(), order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeLoss(int id, int oid) {
        TProductOrder order = productOrderRepository.find(oid);
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
        if (!checkService.checkRolePermission(id, product_loss)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_product_loss_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(order.getId())) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 减少库存
        msg = storageStockService.addStock(id, false, order.getSid(), TypeDefine.OrderType.valueOf(order.getOtype()), oid, 0);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    private RestResult check(int id, TProductOrder order, int applyPerm, int reviewPerm, List<Integer> reviews) {
        // 验证公司
        int gid = order.getGid();
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 校验申请订单权限
        return reviewService.checkPerm(id, gid, applyPerm, reviewPerm, reviews);
    }

    private RestResult createProductComms(TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> values, List<TProductCommodity> list) {
        // 生成进货单
        int size = commoditys.size();
        if (size != types.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            TypeDefine.CommodityType type = TypeDefine.CommodityType.valueOf(types.get(i));
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
                default:
                    return RestResult.fail("商品类型异常：" + type);
            }

            // 生成数据
            TProductCommodity c = new TProductCommodity();
            c.setCtype(type.getValue());
            c.setCid(cid);
            c.setValue(values.get(i));
            list.add(c);
            total = total + unit * values.get(i);
        }
        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }
}
