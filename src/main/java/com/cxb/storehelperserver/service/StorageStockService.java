package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TStorageCommodity;
import com.cxb.storehelperserver.model.TStorageOrder;
import com.cxb.storehelperserver.model.TStorageOrderCommodity;
import com.cxb.storehelperserver.model.TStorageOriginal;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.StorageCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;

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
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageOrderCommodityRepository storageOrderCommodityRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductOrderCommodityRepository productOrderCommodityRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private AgreementOrderCommodityRepository agreementOrderCommodityRepository;

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

    /*
     * desc: 原料进货
     */
    public RestResult purchase(int id, TStorageOrder order, List<Integer> types, List<Integer> commoditys,
                               List<Integer> units, List<Integer> values, List<BigDecimal> prices) {
        // 验证公司
        String msg = checkService.checkGroup(id, order.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 生成进货单
        int size = commoditys.size();
        if (size != types.size() || size != units.size() || size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        int total = 0;
        val list = new ArrayList<TStorageOrderCommodity>();
        for (int i = 0; i < size; i++) {
            TStorageOrderCommodity c = new TStorageOrderCommodity();
            c.setCtype(types.get(i));
            c.setCid(commoditys.get(i));
            c.setUnit(units.get(i));
            c.setValue(values.get(i));
            c.setPrice(prices.get(i));
            list.add(c);
            total += values.get(i);
        }
        order.setValue(total);
        order.setApply(id);
        if (!storageOrderRepository.insert(order)) {
            return RestResult.fail("生成进货订单失败");
        }
        int oid = order.getId();
        int sid = order.getSid();
        for (TStorageOrderCommodity c : list) {
            c.setOrid(oid);
            if (!storageOrderCommodityRepository.insert(c)) {
                return RestResult.fail("生成订单商品数据失败");
            }

            // 修改库存数量
            switch (CommodityType.valueOf(c.getCtype())) {
                case COMMODITY:
                    if (!updateCommodity(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                case HALFGOOD:
                    if (!updateHalfgood(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                case ORIGINAL:
                    if (!updateOriginal(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                case STANDARD:
                    if (!updateStandard(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
                default:
                    if (!updateDestroy(sid, c.getId(), c.getUnit(), c.getValue())) {
                        return RestResult.fail("修改商品库存数据失败");
                    }
                    break;
            }

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

    private boolean updateCommodity(int sid, int cid, int unit, int value) {
        TStorageCommodity storageCommodity = storageCommodityRepository.find(sid, cid);
        if (null == storageCommodity) {
            storageCommodity = new TStorageCommodity();
            storageCommodity.setSid(sid);
            storageCommodity.setCid(cid);
            storageCommodity.setValue(unit * value);
            if (!storageCommodityRepository.insert(storageCommodity)) {
                // TODO 测试写入失败回滚
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
            setOriginalCache(sid, cid, unit * value);
        } else {
            storageCommodity.setValue(storageCommodity.getValue() + (unit * value));
            if (!storageCommodityRepository.update(storageCommodity)) {
                // TODO 测试写入失败回滚
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
            setCommodityCache(sid, cid, unit * value);
        }
        return true;
    }

    private boolean updateHalfgood(int sid, int cid, int unit, int value) {
        return false;
    }

    private boolean updateOriginal(int sid, int cid, int unit, int value) {
        TStorageOriginal storageOriginal = storageOriginalRepository.find(sid, cid);
        if (null == storageOriginal) {
            storageOriginal = new TStorageOriginal();
            storageOriginal.setSid(sid);
            storageOriginal.setOid(cid);
            storageOriginal.setValue(unit * value);
            if (!storageOriginalRepository.insert(storageOriginal)) {
                // TODO 测试写入失败回滚
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
            setOriginalCache(sid, cid, unit * value);
        } else {
            storageOriginal.setValue(storageOriginal.getValue() + (unit * value));
            if (!storageOriginalRepository.update(storageOriginal)) {
                // TODO 测试写入失败回滚
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
            setOriginalCache(sid, cid, unit * value);
        }
        return true;
    }

    private boolean updateStandard(int sid, int cid, int unit, int value) {
        return false;
    }

    private boolean updateDestroy(int sid, int cid, int unit, int value) {
        return false;
    }
}
