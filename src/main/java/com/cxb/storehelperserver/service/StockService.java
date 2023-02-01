package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;

/**
 * desc: 库存统计业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StockService {
    @Resource
    private StockCommodityRepository stockCommodityRepository;

    @Resource
    private StockHalfgoodRepository stockHalfgoodRepository;

    @Resource
    private StockOriginalRepository stockOriginalRepository;

    @Resource
    private StockStandardRepository stockStandardRepository;

    @Resource
    private StockDestroyRepository stockDestroyRepository;

    @Resource
    private AgreementOrderCommodityRepository agreementOrderCommodityRepository;

    @Resource
    private ProductOrderCommodityRepository productOrderCommodityRepository;

    @Resource
    private StorageOrderCommodityRepository storageOrderCommodityRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.stockspan}")
    private int stockspan;

    private static final Object lock = new Object();

    public RestResult getStockCommodity(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockCommodityRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockCommodityRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取商品信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockHalfgood(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockHalfgoodRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockHalfgoodRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取半成品信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockOriginal(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockOriginalRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockOriginalRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取原料信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockStandard(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockStandardRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockStandardRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取标品信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    public RestResult getStockDestroy(int id, int sid, int page, int limit, Date date, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        int total = stockDestroyRepository.total(sid, date, search);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val commodities = stockDestroyRepository.pagination(sid, page, limit, date, search);
        if (null == commodities) {
            return RestResult.fail("获取废料信息失败");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", commodities);
        return RestResult.ok(data);
    }

    /**
     * desc: 计算库存只到昨天，当天的要到晚上12点以后截止
     */
    public RestResult countStock(int id, int sid) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 获取开始时间，若没有记录就查找最初订单
        Date last = getLastStock(sid);
        if (null == last) {
            TUserOrderComplete userOrderComplete = userOrderCompleteRepository.findFirstOrder(sid);
            if (null == userOrderComplete) {
                return RestResult.fail("未查询到订单信息，无法计算库存");
            }
            last = userOrderComplete.getCdate();
        } else {
            // 已生成的库存，日期加1
            dateUtil.addOneDay(last, 1);
        }

        // 计算间隔天数
        Date today = dateUtil.getStartTime(new Date());
        int span = (int) ((today.getTime() - last.getTime()) / (24 * 60 * 60 * 1000));
        if (span <= 0) {
            return RestResult.fail("没有需要计算的库存");
        }
        val comms = new HashMap<Integer, TStockCommodity>();
        val halfs = new HashMap<Integer, TStockHalfgood>();
        val oris = new HashMap<Integer, TStockOriginal>();
        val stans = new HashMap<Integer, TStockStandard>();
        val dests = new HashMap<Integer, TStockDestroy>();
        synchronized (lock) {
            for (int i = 0; i < span; i++, last = dateUtil.addOneDay(last, 1)) {
                ret = countStockOneDay(sid, last, comms, halfs, oris, stans, dests);
                if (null != ret) {
                    return ret;
                }
            }
            try {
                Thread.sleep(stockspan);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
        return RestResult.ok();
    }

    public void delStock(int sid, Date date) {
        stockCommodityRepository.delete(sid, date);
        stockHalfgoodRepository.delete(sid, date);
        stockOriginalRepository.delete(sid, date);
        stockStandardRepository.delete(sid, date);
        stockDestroyRepository.delete(sid, date);
    }

    private RestResult check(int id, int sid) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        TStorage storage = storageRepository.find(sid);
        if (null == storage) {
            return RestResult.fail("获取仓库信息失败");
        }
        if (!group.getGid().equals(storage.getGid())) {
            return RestResult.fail("只能获取本公司信息");
        }
        return null;
    }

    /**
     * desc: 获取库存最后时间
     */
    private Date getLastStock(int sid) {
        Date last = null;
        TStockCommodity commodity = stockCommodityRepository.findLast(sid, 0);
        if (null != commodity) {
            last = commodity.getCdate();
        }
        TStockHalfgood halfgood = stockHalfgoodRepository.findLast(sid, 0);
        if (null != halfgood) {
            if (null == last) {
                last = halfgood.getCdate();
            } else {
                if (halfgood.getCdate().after(last)) {
                    last = halfgood.getCdate();
                }
            }
        }
        TStockOriginal original = stockOriginalRepository.findLast(sid, 0);
        if (null != original) {
            if (null == last) {
                last = original.getCdate();
            } else {
                if (original.getCdate().after(last)) {
                    last = original.getCdate();
                }
            }
        }
        TStockStandard standard = stockStandardRepository.findLast(sid, 0);
        if (null != standard) {
            if (null == last) {
                last = standard.getCdate();
            } else {
                if (standard.getCdate().after(last)) {
                    last = standard.getCdate();
                }
            }
        }
        TStockDestroy destroy = stockDestroyRepository.findLast(sid, 0);
        if (null != destroy) {
            if (null == last) {
                last = destroy.getCdate();
            } else {
                if (destroy.getCdate().after(last)) {
                    last = destroy.getCdate();
                }
            }
        }
        return last;
    }

    /**
     * desc: 处理date当天已审核订单的商品
     */
    private RestResult countStockOneDay(int sid, Date date, HashMap<Integer, TStockCommodity> comms,
                                        HashMap<Integer, TStockHalfgood> halfs, HashMap<Integer, TStockOriginal> oris,
                                        HashMap<Integer, TStockStandard> stans, HashMap<Integer, TStockDestroy> dests) {
        Date start = dateUtil.getStartTime(date);
        Date end = dateUtil.getEndTime(date);
        val agreementOrderCommodities = agreementOrderCommodityRepository.findBySid(sid, start, end);
        handleCommoditys(sid, start, agreementOrderCommodities, comms, halfs, oris, stans, dests);
        val productOrderCommodities = productOrderCommodityRepository.findBySid(sid, start, end);
        handleCommoditys(sid, start, productOrderCommodities, comms, halfs, oris, stans, dests);
        val storageOrderCommodities = storageOrderCommodityRepository.findBySid(sid, start, end);
        handleCommoditys(sid, start, storageOrderCommodities, comms, halfs, oris, stans, dests);

        // 只插入与start相同的记录
        for (Map.Entry<Integer, TStockCommodity> entry : comms.entrySet()) {
            TStockCommodity commodity = entry.getValue();
            if (commodity.getCdate().equals(start)) {
                if (!stockCommodityRepository.insert(commodity)) {
                    SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                    return RestResult.fail("添加商品" + simpleDateFormat.format(commodity.getCdate()) + "库存记录失败" + commodity.getCid());
                }
            }
        }
        for (Map.Entry<Integer, TStockHalfgood> entry : halfs.entrySet()) {
            TStockHalfgood halfgood = entry.getValue();
            if (halfgood.getCdate().equals(start)) {
                if (!stockHalfgoodRepository.insert(halfgood)) {
                    SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                    return RestResult.fail("添加半成品" + simpleDateFormat.format(halfgood.getCdate()) + "库存记录失败" + halfgood.getHid());
                }
            }
        }
        for (Map.Entry<Integer, TStockOriginal> entry : oris.entrySet()) {
            TStockOriginal original = entry.getValue();
            if (original.getCdate().equals(start)) {
                if (!stockOriginalRepository.insert(original)) {
                    SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                    return RestResult.fail("添加原料" + simpleDateFormat.format(original.getCdate()) + "库存记录失败" + original.getOid());
                }
            }
        }
        for (Map.Entry<Integer, TStockStandard> entry : stans.entrySet()) {
            TStockStandard standard = entry.getValue();
            if (standard.getCdate().equals(start)) {
                if (!stockStandardRepository.insert(standard)) {
                    SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                    return RestResult.fail("添加标品" + simpleDateFormat.format(standard.getCdate()) + "库存记录失败" + standard.getSid());
                }
            }
        }
        for (Map.Entry<Integer, TStockDestroy> entry : dests.entrySet()) {
            TStockDestroy destroy = entry.getValue();
            if (destroy.getCdate().equals(start)) {
                if (!stockDestroyRepository.insert(destroy)) {
                    SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                    return RestResult.fail("添加废料" + simpleDateFormat.format(destroy.getCdate()) + "库存记录失败" + destroy.getDid());
                }
            }
        }
        return null;
    }

    private void handleCommoditys(int sid, Date date, List<MyOrderCommodity> commodities, HashMap<Integer, TStockCommodity> comms,
                                  HashMap<Integer, TStockHalfgood> halfs, HashMap<Integer, TStockOriginal> oris,
                                  HashMap<Integer, TStockStandard> stans, HashMap<Integer, TStockDestroy> dests) {
        for (MyOrderCommodity commodity : commodities) {
            switch (CommodityType.valueOf(commodity.getCtype())) {
                case COMMODITY: {
                    TStockCommodity stockCommodity = comms.get(commodity.getCid());
                    if (null == stockCommodity) {
                        // 没数据就先尝试从库存获取
                        stockCommodity = stockCommodityRepository.findLast(sid, commodity.getCid());
                        if (null == stockCommodity) {
                            stockCommodity = new TStockCommodity();
                            stockCommodity.setValue(commodity.getValue());
                            comms.put(commodity.getCid(), stockCommodity);
                        } else {
                            if (commodity.getIo()) {
                                stockCommodity.setValue(stockCommodity.getValue() - commodity.getValue());
                            } else {
                                stockCommodity.setValue(stockCommodity.getValue() + commodity.getValue());
                            }
                            comms.put(commodity.getCid(), stockCommodity);
                        }
                    } else {
                        if (commodity.getIo()) {
                            stockCommodity.setValue(stockCommodity.getValue() - commodity.getValue());
                        } else {
                            stockCommodity.setValue(stockCommodity.getValue() + commodity.getValue());
                        }
                    }
                    stockCommodity.setSid(sid);
                    stockCommodity.setCid(commodity.getCid());
                    stockCommodity.setUnit(commodity.getUnit());
                    stockCommodity.setPrice(commodity.getPrice());
                    stockCommodity.setCdate(date);
                    break;
                }
                case HALFGOOD: {
                    TStockHalfgood stockHalfgood = halfs.get(commodity.getCid());
                    if (null == stockHalfgood) {
                        // 没数据就先尝试从库存获取
                        stockHalfgood = stockHalfgoodRepository.findLast(sid, commodity.getCid());
                        if (null == stockHalfgood) {
                            stockHalfgood = new TStockHalfgood();
                            stockHalfgood.setValue(commodity.getValue());
                            halfs.put(commodity.getCid(), stockHalfgood);
                        } else {
                            if (commodity.getIo()) {
                                stockHalfgood.setValue(stockHalfgood.getValue() - commodity.getValue());
                            } else {
                                stockHalfgood.setValue(stockHalfgood.getValue() + commodity.getValue());
                            }
                            halfs.put(commodity.getCid(), stockHalfgood);
                        }
                    } else {
                        if (commodity.getIo()) {
                            stockHalfgood.setValue(stockHalfgood.getValue() - commodity.getValue());
                        } else {
                            stockHalfgood.setValue(stockHalfgood.getValue() + commodity.getValue());
                        }
                    }
                    stockHalfgood.setSid(sid);
                    stockHalfgood.setHid(commodity.getCid());
                    stockHalfgood.setUnit(commodity.getUnit());
                    stockHalfgood.setPrice(commodity.getPrice());
                    stockHalfgood.setCdate(date);
                    break;
                }
                case ORIGINAL: {
                    TStockOriginal stockOriginal = oris.get(commodity.getCid());
                    if (null == stockOriginal) {
                        // 没数据就先尝试从库存获取
                        stockOriginal = stockOriginalRepository.findLast(sid, commodity.getCid());
                        if (null == stockOriginal) {
                            stockOriginal = new TStockOriginal();
                            stockOriginal.setValue(commodity.getValue());
                            oris.put(commodity.getCid(), stockOriginal);
                        } else {
                            if (commodity.getIo()) {
                                stockOriginal.setValue(stockOriginal.getValue() - commodity.getValue());
                            } else {
                                stockOriginal.setValue(stockOriginal.getValue() + commodity.getValue());
                            }
                            oris.put(commodity.getCid(), stockOriginal);
                        }
                    } else {
                        if (commodity.getIo()) {
                            stockOriginal.setValue(stockOriginal.getValue() - commodity.getValue());
                        } else {
                            stockOriginal.setValue(stockOriginal.getValue() + commodity.getValue());
                        }
                    }
                    stockOriginal.setSid(sid);
                    stockOriginal.setOid(commodity.getCid());
                    stockOriginal.setUnit(commodity.getUnit());
                    stockOriginal.setPrice(commodity.getPrice());
                    stockOriginal.setCdate(date);
                    break;
                }
                case STANDARD: {
                    TStockStandard stockStandard = stans.get(commodity.getCid());
                    if (null == stockStandard) {
                        // 没数据就先尝试从库存获取
                        stockStandard = stockStandardRepository.findLast(sid, commodity.getCid());
                        if (null == stockStandard) {
                            stockStandard = new TStockStandard();
                            stockStandard.setValue(commodity.getValue());
                            stans.put(commodity.getCid(), stockStandard);
                        } else {
                            if (commodity.getIo()) {
                                stockStandard.setValue(stockStandard.getValue() - commodity.getValue());
                            } else {
                                stockStandard.setValue(stockStandard.getValue() + commodity.getValue());
                            }
                            stans.put(commodity.getCid(), stockStandard);
                        }
                    } else {
                        if (commodity.getIo()) {
                            stockStandard.setValue(stockStandard.getValue() - commodity.getValue());
                        } else {
                            stockStandard.setValue(stockStandard.getValue() + commodity.getValue());
                        }
                    }
                    stockStandard.setSid(sid);
                    stockStandard.setSid(commodity.getCid());
                    stockStandard.setUnit(commodity.getUnit());
                    stockStandard.setPrice(commodity.getPrice());
                    stockStandard.setCdate(date);
                    break;
                }
                case DESTROY: {
                    TStockDestroy stockDestroy = dests.get(commodity.getCid());
                    if (null == stockDestroy) {
                        // 没数据就先尝试从库存获取
                        stockDestroy = stockDestroyRepository.findLast(sid, commodity.getCid());
                        if (null == stockDestroy) {
                            stockDestroy = new TStockDestroy();
                            stockDestroy.setValue(commodity.getValue());
                            dests.put(commodity.getCid(), stockDestroy);
                        } else {
                            if (commodity.getIo()) {
                                stockDestroy.setValue(stockDestroy.getValue() - commodity.getValue());
                            } else {
                                stockDestroy.setValue(stockDestroy.getValue() + commodity.getValue());
                            }
                            dests.put(commodity.getCid(), stockDestroy);
                        }
                    } else {
                        if (commodity.getIo()) {
                            stockDestroy.setValue(stockDestroy.getValue() - commodity.getValue());
                        } else {
                            stockDestroy.setValue(stockDestroy.getValue() + commodity.getValue());
                        }
                    }
                    stockDestroy.setSid(sid);
                    stockDestroy.setSid(commodity.getCid());
                    stockDestroy.setUnit(commodity.getUnit());
                    stockDestroy.setPrice(commodity.getPrice());
                    stockDestroy.setCdate(date);
                    break;
                }
            }
        }
    }
}
