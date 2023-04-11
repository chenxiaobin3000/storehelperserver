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
import static com.cxb.storehelperserver.util.TypeDefine.ProductType.*;

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
    private StockService stockService;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductCommodityRepository productCommodityRepository;

    @Resource
    private ProductAttachmentRepository productAttachmentRepository;

    @Resource
    private ProductRemarkRepository productRemarkRepository;

    @Resource
    private HalfgoodOriginalRepository halfgoodOriginalRepository;

    @Resource
    private CommodityOriginalRepository commodityOriginalRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private StockDayRepository stockDayRepository;

    @Resource
    private DateUtil dateUtil;

    /**
     * desc: 生产订单
     */
    public RestResult collect(int id, TProductOrder order, List<Integer> types, List<Integer> commoditys, List<Integer> weights, List<Integer> values,
                              List<Integer> types2, List<Integer> commoditys2, List<BigDecimal> prices2, List<Integer> weights2, List<Integer> values2,
                              List<Integer> types3, List<Integer> commoditys3, List<Integer> weights3, List<Integer> values3, List<Integer> attrs) {
        val reviews = new ArrayList<Integer>();
        RestResult ret = check(id, order, mp_product_collect_apply, mp_product_collect_review, reviews);
        if (null != ret) {
            return ret;
        }

        // 校验完成商品关联生产原料
        int weight = 0;
        for (Integer w : weights) {
            weight += w;
        }
        for (Integer w : weights2) {
            weight -= w;
        }
        for (Integer w : weights3) {
            weight -= w;
        }
        if (0 != weight) {
            return RestResult.fail("出入库总重量必须相等");
        }

        // 生成生产单
        val comms = new ArrayList<TProductCommodity>();
        ret = createCollectComms(order, PRODUCT_OUT.getValue(), types, commoditys, null, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 完成商品
        val comms2 = new ArrayList<TProductCommodity>();
        ret = createCollectComms(order, PRODUCT_IN.getValue(), types2, commoditys2, prices2, weights2, values2, comms);
        if (null != ret) {
            return ret;
        }
        comms.addAll(comms2);

        // 损耗商品
        val comms3 = new ArrayList<TProductCommodity>();
        if (!types3.isEmpty()) {
            ret = createCollectComms(order, PRODUCT_LOSS.getValue(), types3, commoditys3, null, weights3, values3, comms);
            if (null != ret) {
                return ret;
            }
            comms.addAll(comms3);
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
     * desc: 生产订单修改
     */
    public RestResult setCollect(int id, int oid, int sid, Date applyTime, List<Integer> types, List<Integer> commoditys, List<Integer> weights,
                                 List<Integer> values, List<Integer> types2, List<Integer> commoditys2, List<BigDecimal> prices2, List<Integer> weights2,
                                 List<Integer> values2, List<Integer> types3, List<Integer> commoditys3, List<Integer> weights3, List<Integer> values3, List<Integer> attrs) {
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
        RestResult ret = check(id, order, mp_product_collect_apply, mp_product_collect_review, reviews);
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
        ret = createCollectComms(order, PRODUCT_OUT.getValue(), types, commoditys, null, weights, values, comms);
        if (null != ret) {
            return ret;
        }

        // 完成商品
        val comms2 = new ArrayList<TProductCommodity>();
        ret = createCollectComms(order, PRODUCT_IN.getValue(), types2, commoditys2, prices2, weights2, values2, comms);
        if (null != ret) {
            return ret;
        }
        comms.addAll(comms2);

        // 损耗商品
        val comms3 = new ArrayList<TProductCommodity>();
        if (!types3.isEmpty()) {
            ret = createCollectComms(order, PRODUCT_LOSS.getValue(), types3, commoditys3, null, weights3, values3, comms);
            if (null != ret) {
                return ret;
            }
            comms.addAll(comms3);
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

    public RestResult delCollect(int id, int oid) {
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

    public RestResult reviewCollect(int id, int oid) {
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
        String msg = stockService.handleProductStock(order, false);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return reviewService.review(order.getApply(), id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApplyTime());
    }

    public RestResult revokeCollect(int id, int oid) {
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
        if (!checkService.checkRolePermission(id, product_collect)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        RestResult ret = reviewService.revoke(id, gid, order.getSid(), order.getOtype(), oid, order.getBatch(), order.getApply(), mp_product_collect_review);
        if (null != ret) {
            return ret;
        }

        // 撤销审核人信息
        if (!productOrderRepository.setReviewNull(oid)) {
            return RestResult.fail("撤销订单审核信息失败");
        }

        // 增加库存
        msg = stockService.handleProductStock(order, true);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        return RestResult.ok();
    }

    public RestResult addCollectInfo(int id, int oid, String remark) {
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

    public RestResult delCollectInfo(int id, int oid, int rid) {
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

    private RestResult createCollectComms(TProductOrder order, int iotype, List<Integer> types, List<Integer> commoditys,
                                          List<BigDecimal> prices, List<Integer> weights, List<Integer> values, List<TProductCommodity> list) {
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
            TStockDay stock = stockDayRepository.findByYesterday(sid, ctype, cid);
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
            c.setIotype(iotype);
            c.setCtype(ctype);
            c.setCid(cid);
            if (PRODUCT_IN.getValue() == iotype) {
                c.setPrice(prices.get(i));
            } else {
                c.setPrice(stock.getPrice().multiply(new BigDecimal(weight)).divide(new BigDecimal(stock.getWeight()), 2, RoundingMode.DOWN));
            }
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
}
