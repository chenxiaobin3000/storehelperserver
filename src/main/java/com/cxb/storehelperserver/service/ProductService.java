package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.service.model.OriginalIndex;
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
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.*;
import static com.cxb.storehelperserver.util.TypeDefine.OrderType.PRODUCT_PROCESS_ORDER;

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
    private ProductRemarkRepository productRemarkRepository;

    @Resource
    private ProductCompleteRepository productCompleteRepository;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private StockRepository stockRepository;

    @Resource
    private HalfgoodOriginalRepository halfgoodOriginalRepository;

    @Resource
    private CommodityOriginalRepository commodityOriginalRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 生产开始
     */
    public RestResult process(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_process_apply, mp_product_process_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成生产单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProcessComms(order, types, commoditys, weights, values, comms);
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
    public RestResult setProcess(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_process_apply, mp_product_process_review, reviews);
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

        // 生成生产单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProcessComms(order, types, commoditys, weights, values, comms);
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
        int gid = group.getGid();

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

        // 减少库存
        String msg = storageStockService.handleProductStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
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
        if (!productOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = storageStockService.handleProductStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addProcessInfo(int id, int oid, String remark) {
        // 验证公司
        TProductOrder order = productOrderRepository.find(oid);
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
        productOrderService.clean(oid);

        // 备注
        if (null != remark && remark.length() > 0) {
            if (!productRemarkRepository.insert(oid, remark, new Date())) {
                return RestResult.fail("添加备注失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delProcessInfo(int id, int oid, int rid) {
        // 验证公司
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到订单信息");
        }
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }
        productOrderService.clean(oid);

        // 备注由审核人删
        if (0 != rid) {
            if (!order.getReview().equals(rid)) {
                RestResult.fail("要删除备注，请联系订单审核人");
            }
            if (!productRemarkRepository.delete(rid)) {
                return RestResult.fail("删除备注信息失败");
            }
        }
        return RestResult.ok();
    }

    /**
     * desc: 生产完成
     */
    public RestResult complete(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 生产单未审核不能完成
        int pid = order.getPid();
        TProductOrder process = productOrderRepository.find(pid);
        if (null == process) {
            return RestResult.fail("未查询到生产单");
        }
        if (!process.getOtype().equals(PRODUCT_PROCESS_ORDER.getValue())) {
            return RestResult.fail("生产单据类型异常");
        }
        if (null == process.getReview()) {
            return RestResult.fail("生产单未审核通过，不能进行入库");
        }

        order.setGid(process.getGid());
        order.setSid(process.getSid());
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_complete_apply, mp_product_complete_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成完成单
        val comms = new ArrayList<TProductCommodity>();
        ret = createCompleteComms(order, types, commoditys, weights, values, comms);
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
    public RestResult setComplete(int id, int oid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_complete_apply, mp_product_complete_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成进货单
        val comms = new ArrayList<TProductCommodity>();
        ret = createCompleteComms(order, types, commoditys, weights, values, comms);
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
        productAttachmentRepository.deleteByOid(oid);
        if (!productCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
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
        int gid = group.getGid();

        // 校验审核人员信息
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要审核的订单");
        }
        if (!reviewService.checkReview(id, order.getOtype(), oid)) {
            return RestResult.fail("您没有审核权限");
        }

        // 校验完成订单总量不能超出开始单
        TProductOrder process = productOrderRepository.find(order.getPid());
        if (null == process) {
            return RestResult.fail("未查询到对应的生产单");
        }
        int unit = process.getCurUnit() - order.getUnit();
        if (unit < 0) {
            return RestResult.fail("完成商品总量不能超出生产订单总量");
        }
        if (0 == unit) {
            process.setComplete(new Byte("1"));
        }
        process.setCurUnit(unit);
        if (!productOrderRepository.update(process)) {
            return RestResult.fail("修改生产单数据失败");
        }

        // 添加审核信息
        Date reviewTime = new Date();
        order.setReview(id);
        order.setReviewTime(reviewTime);
        if (!productOrderRepository.update(order)) {
            return RestResult.fail("审核用户订单信息失败");
        }

        // 添加关联
        if (!productCompleteRepository.insert(oid, order.getPid())) {
            return RestResult.fail("添加生产完成信息失败");
        }

        // 增加库存
        String msg = storageStockService.handleCompleteStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
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

        // TODO 还原扣除的采购单数量

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_product_complete_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 删除关联
        if (!productCompleteRepository.delete(oid, order.getPid())) {
            return RestResult.fail("撤销生产完成信息失败");
        }

        // 减少库存
        msg = storageStockService.handleCompleteStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addCompleteInfo(int id, int oid, String remark) {
        return addProcessInfo(id, oid, remark);
    }

    public RestResult delCompleteInfo(int id, int oid, int rid) {
        return delProcessInfo(id, oid, rid);
    }

    /**
     * desc: 生产损耗
     */
    public RestResult loss(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_loss_apply, mp_product_loss_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 生成损耗单
        val comms = new ArrayList<TProductCommodity>();
        ret = createProcessComms(order, types, commoditys, weights, values, comms);
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
        return reviewService.apply(id, order.getGid(), order.getSid(), order.getOtype(), oid, batch, reviews);
    }

    /**
     * desc: 仓储损耗修改
     */
    public RestResult setLoss(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<Integer> attrs) {
        // 已经审核的订单不能修改
        TProductOrder order = productOrderRepository.find(oid);
        if (null == order) {
            return RestResult.fail("未查询到要删除的订单");
        }
        if (null != order.getReview()) {
            return RestResult.fail("已审核的订单不能修改");
        }
        if (!order.getApply().equals(id)) {
            return RestResult.fail("只能修改自己的订单");
        }

        order.setApplyTime(applyTime);
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_loss_apply, mp_product_loss_review, reviews);
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
        val comms = new ArrayList<TProductCommodity>();
        ret = createProcessComms(order, types, commoditys, weights, values, comms);
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
        productAttachmentRepository.deleteByOid(oid);
        if (!productCommodityRepository.delete(oid)) {
            return RestResult.fail("删除关联商品失败");
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
        int gid = group.getGid();

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

        // 减少库存
        String msg = storageStockService.handleProductStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
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
        if (!productOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = storageStockService.handleProductStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addLossInfo(int id, int oid, String remark) {
        return addProcessInfo(id, oid, remark);
    }

    public RestResult delLossInfo(int id, int oid, int rid) {
        return delProcessInfo(id, oid, rid);
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

    private RestResult createProcessComms(TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TProductCommodity> list) {
        // 生成进货单
        int size = commoditys.size();
        if (size != types.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        int sid = order.getSid();
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            // 获取商品单位信息
            int ctype = types.get(i);
            int cid = commoditys.get(i);
            int weight = weights.get(i);
            int value = values.get(i);
            TStock stock = stockRepository.find(sid, ctype, cid);
            if (null == stock) {
                return RestResult.fail("未查询到库存类型:" + types.get(i) + ",商品:" + commoditys.get(i));
            }
            if (weight > stock.getWeight()) {
                return RestResult.fail("库存商品重量不足:" + ctype + ",商品:" + cid);
            }
            if (value > stock.getValue()) {
                return RestResult.fail("库存商品件数不足:" + ctype + ",商品:" + cid);
            }

            TProductCommodity c = new TProductCommodity();
            c.setCtype(ctype);
            c.setCid(cid);
            c.setPrice(stock.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(stock.getWeight()), 2, RoundingMode.DOWN));
            c.setWeight(weight);
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

    private RestResult createCompleteComms(TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values, List<TProductCommodity> list) {
        // 生成进货单
        int size = commoditys.size();
        if (size != types.size() || size != weights.size() || size != values.size()) {
            return RestResult.fail("商品信息异常");
        }
        val productCommodities = productCommodityRepository.find(order.getPid());
        if (null == productCommodities || productCommodities.isEmpty()) {
            return RestResult.fail("未查询到生产原料信息");
        }
        // 生成原料索引表
        val originalTable = new HashMap<Integer, OriginalIndex>();
        for (TProductCommodity pc : productCommodities) {
            if (pc.getCtype().equals(ORIGINAL.getValue())) {
                originalTable.put(pc.getCid(), new OriginalIndex(pc));
            }
        }

        // 补充出库半成品信息
        for (TProductCommodity pc : productCommodities) {
            if (pc.getCtype().equals(HALFGOOD.getValue())) {
                OriginalIndex original = originalTable.get(pc.getCid());
                if (null == original) {
                    // 只有半成品，没有原料，就补充原料信息
                    THalfgoodOriginal halfgood = halfgoodOriginalRepository.find(pc.getCid());
                    if (null == halfgood) {
                        return RestResult.fail("未查询到半成品对应的原料信息：" + pc.getCid());
                    }
                    TProductCommodity c = new TProductCommodity();
                    c.setCtype(ORIGINAL.getValue());
                    c.setCid(halfgood.getOid());
                    c.setPrice(new BigDecimal(0));
                    c.setWeight(0);
                    c.setValue(0);
                    original = new OriginalIndex(c);
                    original.setHalfgood(c);
                    originalTable.put(pc.getCid(), original);
                } else {
                    if (null != original.getHalfgood()) {
                        return RestResult.fail("一个订单只能存在单一原料系列：" + pc.getCid());
                    }
                    original.setHalfgood(pc);
                }
            }
        }

        // 从原料开始扣减
        int total = 0;
        BigDecimal price = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            int ctype = types.get(i);
            if (ctype == ORIGINAL.getValue()) {
                int cid = commoditys.get(i);
                int weight = weights.get(i);
                OriginalIndex originalIndex = originalTable.get(cid);
                if (null == originalIndex) {
                    return RestResult.fail("未查询到原料id:" + cid);
                }
                TProductCommodity original = originalIndex.getOriginal();
                if (weight > original.getWeight()) {
                    return RestResult.fail("入库原料重量不能大于生产重量:" + cid);
                }

                TProductCommodity c = new TProductCommodity();
                c.setCtype(ctype);
                c.setCid(cid);
                if (weight == original.getWeight()) {
                    c.setPrice(original.getPrice());
                } else {
                    c.setPrice(original.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(original.getWeight()), 2, RoundingMode.DOWN));
                }
                c.setWeight(weight);
                c.setValue(values.get(i));
                list.add(c);
                originalIndex.setOriginal2(c);

                total = total + weight;
                price = price.add(c.getPrice());
            }
        }

        // 然后扣减半成品
        for (int i = 0; i < size; i++) {
            int ctype = types.get(i);
            if (ctype == HALFGOOD.getValue()) {
                int cid = commoditys.get(i);
                int weight = weights.get(i);
                THalfgoodOriginal halfgoodOriginal = halfgoodOriginalRepository.find(cid);
                if (null == halfgoodOriginal) {
                    return RestResult.fail("未查询到半成品对应的原料信息：" + cid);
                }
                OriginalIndex originalIndex = originalTable.get(halfgoodOriginal.getOid());
                if (null == originalIndex) {
                    return RestResult.fail("未查询到原料id:" + cid);
                }
                TProductCommodity original = originalIndex.getOriginal();
                TProductCommodity halfgood = originalIndex.getHalfgood();
                TProductCommodity original2 = originalIndex.getOriginal2();
                int weight1 = weight + (null == original2 ? 0 : original2.getWeight());
                int weight2 = original.getWeight() + (null == halfgood ? 0 : halfgood.getWeight());
                if (weight1 > weight2) {
                    return RestResult.fail("入库半成品重量不能大于生产重量:" + cid);
                }

                TProductCommodity c = new TProductCommodity();
                c.setCtype(ctype);
                c.setCid(cid);
                int weightOriginal = original.getWeight() - (null == original2 ? 0 : original2.getWeight()); // 原料剩余重量
                if (weightOriginal > 0) { // 还有原料
                    if (weight <= weightOriginal) { // 原料够用，直接转换
                        c.setPrice(original.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(original.getWeight()), 2, RoundingMode.DOWN));
                    } else {// 原料不够，凑半产品
                        if (null != halfgood) {
                            BigDecimal priceOriginal = original.getPrice().multiply(new BigDecimal(weightOriginal)).divide(new BigDecimal(original.getWeight()), 2, RoundingMode.DOWN);
                            BigDecimal priceHalfgood = halfgood.getPrice().multiply(new BigDecimal(weight - weightOriginal)).divide(new BigDecimal(halfgood.getWeight()), 2, RoundingMode.DOWN);
                            c.setPrice(priceOriginal.add(priceHalfgood));
                        }
                    }
                } else { // 只有半成品
                    if (null != halfgood) {
                        c.setPrice(halfgood.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(halfgood.getWeight()), 2, RoundingMode.DOWN));
                    }
                }
                c.setWeight(weight);
                c.setValue(values.get(i));
                list.add(c);

                total = total + weight;
                price = price.add(c.getPrice());
            }
        }

        // 最后处理商品
        for (int i = 0; i < size; i++) {
            int ctype = types.get(i);
            if (ctype == COMMODITY.getValue()) {
                int cid = commoditys.get(i);
                int weight = weights.get(i);
                TCommodityOriginal commodityOriginal = commodityOriginalRepository.find(cid);
                if (null == commodityOriginal) {
                    return RestResult.fail("未查询到商品对应的原料信息：" + cid);
                }
                OriginalIndex originalIndex = originalTable.get(commodityOriginal.getOid());
                if (null == originalIndex) {
                    return RestResult.fail("未查询到原料id:" + cid);
                }
                TProductCommodity original = originalIndex.getOriginal();
                TProductCommodity halfgood = originalIndex.getHalfgood();
                TProductCommodity original2 = originalIndex.getOriginal2();
                TProductCommodity halfgood2 = originalIndex.getHalfgood2();
                int weight1 = weight + (null == original2 ? 0 : original2.getWeight()) + (null == halfgood2 ? 0 : halfgood2.getWeight());
                int weight2 = original.getWeight() + (null == halfgood ? 0 : halfgood.getWeight());
                if (weight1 > weight2) {
                    return RestResult.fail("入库商品品重量不能大于生产重量:" + cid);
                }

                TProductCommodity c = new TProductCommodity();
                c.setCtype(ctype);
                c.setCid(cid);
                int weightOriginal = original.getWeight() - (null == original2 ? 0 : original2.getWeight()); // 原料剩余重量
                if (weightOriginal > 0) { // 还有原料
                    if (weight <= weightOriginal) { // 原料够用，直接转换
                        c.setPrice(original.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(original.getWeight()), 2, RoundingMode.DOWN));
                    } else {// 原料不够，凑半产品
                        if (null != halfgood) {
                            BigDecimal priceOriginal = original.getPrice().multiply(new BigDecimal(weightOriginal)).divide(new BigDecimal(original.getWeight()), 2, RoundingMode.DOWN);
                            BigDecimal priceHalfgood = halfgood.getPrice().multiply(new BigDecimal(weight - weightOriginal)).divide(new BigDecimal(halfgood.getWeight()), 2, RoundingMode.DOWN);
                            c.setPrice(priceOriginal.add(priceHalfgood));
                        }
                    }
                } else { // 只有半成品
                    if (null != halfgood) {
                        c.setPrice(halfgood.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(halfgood.getWeight()), 2, RoundingMode.DOWN));
                    }
                }
                c.setWeight(weight);
                c.setValue(values.get(i));
                list.add(c);

                total = total + weight;
                price = price.add(c.getPrice());
            }
        }

        order.setUnit(total);
        order.setPrice(price);
        order.setCurUnit(total);
        order.setCurPrice(price);
        return null;
    }
}
