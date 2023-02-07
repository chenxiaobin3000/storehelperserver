package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.*;
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

import static com.cxb.storehelperserver.util.Permission.admin_grouplist;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;
import static com.cxb.storehelperserver.util.TypeDefine.ReportCycleType;
import static com.cxb.storehelperserver.util.TypeDefine.ReportCycleType.*;

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
    private CheckService checkService;

    @Resource
    private StockCommodityDayRepository stockCommodityDayRepository;

    @Resource
    private StockHalfgoodDayRepository stockHalfgoodDayRepository;

    @Resource
    private StockOriginalDayRepository stockOriginalDayRepository;

    @Resource
    private StockStandardDayRepository stockStandardDayRepository;

    @Resource
    private StockDestroyDayRepository stockDestroyDayRepository;

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

    @Value("${store-app.config.stockday}")
    private int stockday;

    @Value("${store-app.config.stockspan}")
    private int stockspan;

    private static final Object lock = new Object();

    public RestResult getStockCommodity(int id, int sid, Date date, int page, int limit, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 仓库id为0就查找整个公司
        val data = new HashMap<String, Object>();
        if (0 == sid) {
            // 获取公司信息
            TUserGroup group = userGroupRepository.find(id);
            if (null == group) {
                return RestResult.fail("获取公司信息失败");
            }

            int total = stockCommodityDayRepository.totalByGid(group.getGid(), date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockCommodityDayRepository.paginationByGid(group.getGid(), page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取商品信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        } else {
            int total = stockCommodityDayRepository.totalBySid(sid, date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockCommodityDayRepository.paginationBySid(sid, page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取商品信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        }
        return RestResult.ok(data);
    }

    public List<MyStockCommodity> getAllStockCommodity(int gid, int sid, Date date, ReportCycleType type) {
        // 仓库id为0就查找整个公司
        if (0 == sid) {
            int total = stockCommodityDayRepository.totalByGid(gid, date, null);
            if (total > 0) {
                return stockCommodityDayRepository.paginationByGid(gid, 1, total, date, null);
            }
        } else {
            int total = stockCommodityDayRepository.totalBySid(sid, date, null);
            if (total > 0) {
                return stockCommodityDayRepository.paginationBySid(sid, 1, total, date, null);
            }
        }
        return null;
    }

    public RestResult getStockHalfgood(int id, int sid, Date date, int page, int limit, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 仓库id为0就查找整个公司
        val data = new HashMap<String, Object>();
        if (0 == sid) {
            // 获取公司信息
            TUserGroup group = userGroupRepository.find(id);
            if (null == group) {
                return RestResult.fail("获取公司信息失败");
            }

            int total = stockHalfgoodDayRepository.totalByGid(group.getGid(), date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockHalfgoodDayRepository.paginationByGid(group.getGid(), page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取半成品信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        } else {
            int total = stockHalfgoodDayRepository.totalBySid(sid, date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockHalfgoodDayRepository.paginationBySid(sid, page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取半成品信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        }
        return RestResult.ok(data);
    }

    public List<MyStockHalfgood> getAllStockHalfgood(int gid, int sid, Date date, ReportCycleType type) {
        // 仓库id为0就查找整个公司
        if (0 == sid) {
            int total = stockHalfgoodDayRepository.totalByGid(gid, date, null);
            if (total > 0) {
                return stockHalfgoodDayRepository.paginationByGid(gid, 1, total, date, null);
            }
        } else {
            int total = stockHalfgoodDayRepository.totalBySid(sid, date, null);
            if (total > 0) {
                return stockHalfgoodDayRepository.paginationBySid(sid, 1, total, date, null);
            }
        }
        return null;
    }

    public RestResult getStockOriginal(int id, int sid, Date date, int page, int limit, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 仓库id为0就查找整个公司
        val data = new HashMap<String, Object>();
        if (0 == sid) {
            // 获取公司信息
            TUserGroup group = userGroupRepository.find(id);
            if (null == group) {
                return RestResult.fail("获取公司信息失败");
            }

            int total = stockOriginalDayRepository.totalByGid(group.getGid(), date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockOriginalDayRepository.paginationByGid(group.getGid(), page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取原料信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        } else {
            int total = stockOriginalDayRepository.totalBySid(sid, date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockOriginalDayRepository.paginationBySid(sid, page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取原料信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        }
        return RestResult.ok(data);
    }

    public List<MyStockOriginal> getAllStockOriginal(int gid, int sid, Date date, ReportCycleType type) {
        // 仓库id为0就查找整个公司
        if (0 == sid) {
            int total = stockOriginalDayRepository.totalByGid(gid, date, null);
            if (total > 0) {
                return stockOriginalDayRepository.paginationByGid(gid, 1, total, date, null);
            }
        } else {
            int total = stockOriginalDayRepository.totalBySid(sid, date, null);
            if (total > 0) {
                return stockOriginalDayRepository.paginationBySid(sid, 1, total, date, null);
            }
        }
        return null;
    }

    public RestResult getStockStandard(int id, int sid, Date date, int page, int limit, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 仓库id为0就查找整个公司
        val data = new HashMap<String, Object>();
        if (0 == sid) {
            // 获取公司信息
            TUserGroup group = userGroupRepository.find(id);
            if (null == group) {
                return RestResult.fail("获取公司信息失败");
            }

            int total = stockStandardDayRepository.totalByGid(group.getGid(), date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockStandardDayRepository.paginationByGid(group.getGid(), page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取标品信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        } else {
            int total = stockStandardDayRepository.totalBySid(sid, date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockStandardDayRepository.paginationBySid(sid, page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取标品信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        }
        return RestResult.ok(data);
    }

    public List<MyStockStandard> getAllStockStandard(int gid, int sid, Date date, ReportCycleType type) {
        // 仓库id为0就查找整个公司
        if (0 == sid) {
            int total = stockStandardDayRepository.totalByGid(gid, date, null);
            if (total > 0) {
                return stockStandardDayRepository.paginationByGid(gid, 1, total, date, null);
            }
        } else {
            int total = stockStandardDayRepository.totalBySid(sid, date, null);
            if (total > 0) {
                return stockStandardDayRepository.paginationBySid(sid, 1, total, date, null);
            }
        }
        return null;
    }

    public RestResult getStockDestroy(int id, int sid, Date date, int page, int limit, String search) {
        RestResult ret = check(id, sid);
        if (null != ret) {
            return ret;
        }

        // 仓库id为0就查找整个公司
        val data = new HashMap<String, Object>();
        if (0 == sid) {
            // 获取公司信息
            TUserGroup group = userGroupRepository.find(id);
            if (null == group) {
                return RestResult.fail("获取公司信息失败");
            }

            int total = stockDestroyDayRepository.totalByGid(group.getGid(), date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockDestroyDayRepository.paginationByGid(group.getGid(), page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取废料信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        } else {
            int total = stockDestroyDayRepository.totalBySid(sid, date, search);
            if (0 == total) {
                data.put("total", 0);
                data.put("list", null);
                return RestResult.ok(data);
            }

            val commodities = stockDestroyDayRepository.paginationBySid(sid, page, limit, date, search);
            if (null == commodities) {
                return RestResult.fail("获取废料信息失败");
            }
            data.put("total", total);
            data.put("list", commodities);
        }
        return RestResult.ok(data);
    }

    public List<MyStockDestroy> getAllStockDestroy(int gid, int sid, Date date, ReportCycleType type) {
        // 仓库id为0就查找整个公司
        if (0 == sid) {
            int total = stockDestroyDayRepository.totalByGid(gid, date, null);
            if (total > 0) {
                return stockDestroyDayRepository.paginationByGid(gid, 1, total, date, null);
            }
        } else {
            int total = stockDestroyDayRepository.totalBySid(sid, date, null);
            if (total > 0) {
                return stockDestroyDayRepository.paginationBySid(sid, 1, total, date, null);
            }
        }
        return null;
    }

    /**
     * desc: 计算库存只到昨天，当天的要到晚上12点以后截止
     */
    public RestResult countStock(int id, int gid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, admin_grouplist)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        int total = storageRepository.total(gid, null);
        val storages = storageRepository.pagination(gid, 1, total, null);
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        synchronized (lock) {
            for (TStorage storage : storages) {
                val comms = new HashMap<Integer, TStockCommodityDay>();
                val halfs = new HashMap<Integer, TStockHalfgoodDay>();
                val oris = new HashMap<Integer, TStockOriginalDay>();
                val stans = new HashMap<Integer, TStockStandardDay>();
                val dests = new HashMap<Integer, TStockDestroyDay>();

                // 获取开始时间，若没有记录就查找最初订单
                int sid = storage.getId();
                Date last = getLastStock(sid);
                if (null == last) {
                    TUserOrderComplete userOrderComplete = userOrderCompleteRepository.findFirstOrder(sid);
                    if (null == userOrderComplete) {
                        log.info("库存空仓库：" + sid);
                        continue;
                    }
                    last = userOrderComplete.getCdate();
                } else {
                    // 获取库存信息
                    val stockComms = getAllStockCommodity(gid, sid, last, REPORT_DAILY);
                    if (null != stockComms && !stockComms.isEmpty()) {
                        for (MyStockCommodity c : stockComms) {
                            TStockCommodityDay sc = new TStockCommodityDay();
                            sc.setGid(gid);
                            sc.setSid(sid);
                            sc.setCid(c.getCid());
                            sc.setUnit(c.getUnit());
                            sc.setValue(c.getValue());
                            sc.setPrice(c.getPrice());
                            comms.put(c.getCid(), sc);
                        }
                    }
                    val stockHalfs = getAllStockHalfgood(gid, sid, last, REPORT_DAILY);
                    if (null != stockHalfs && !stockHalfs.isEmpty()) {
                        for (MyStockHalfgood c : stockHalfs) {
                            TStockHalfgoodDay sc = new TStockHalfgoodDay();
                            sc.setGid(gid);
                            sc.setSid(sid);
                            sc.setHid(c.getCid());
                            sc.setUnit(c.getUnit());
                            sc.setValue(c.getValue());
                            sc.setPrice(c.getPrice());
                            halfs.put(c.getCid(), sc);
                        }
                    }
                    val stockOris = getAllStockOriginal(gid, sid, last, REPORT_DAILY);
                    if (null != stockOris && !stockOris.isEmpty()) {
                        for (MyStockOriginal c : stockOris) {
                            TStockOriginalDay sc = new TStockOriginalDay();
                            sc.setGid(gid);
                            sc.setSid(sid);
                            sc.setOid(c.getCid());
                            sc.setUnit(c.getUnit());
                            sc.setValue(c.getValue());
                            sc.setPrice(c.getPrice());
                            oris.put(c.getCid(), sc);
                        }
                    }
                    val stockStans = getAllStockStandard(gid, sid, last, REPORT_DAILY);
                    if (null != stockStans && !stockStans.isEmpty()) {
                        for (MyStockStandard c : stockStans) {
                            TStockStandardDay sc = new TStockStandardDay();
                            sc.setGid(gid);
                            sc.setSid(sid);
                            sc.setStid(c.getCid());
                            sc.setUnit(c.getUnit());
                            sc.setValue(c.getValue());
                            sc.setPrice(c.getPrice());
                            stans.put(c.getCid(), sc);
                        }
                    }
                    val stockDests = getAllStockDestroy(gid, sid, last, REPORT_DAILY);
                    if (null != stockDests && !stockDests.isEmpty()) {
                        for (MyStockDestroy c : stockDests) {
                            TStockDestroyDay sc = new TStockDestroyDay();
                            sc.setGid(gid);
                            sc.setSid(sid);
                            sc.setDid(c.getCid());
                            sc.setUnit(c.getUnit());
                            sc.setValue(c.getValue());
                            sc.setPrice(c.getPrice());
                            dests.put(c.getCid(), sc);
                        }
                    }

                    // 已生成的库存，日期加1
                    dateUtil.addOneDay(last, 1);
                }

                // 计算间隔天数
                Date today = dateUtil.getStartTime(new Date());
                int span = (int) ((today.getTime() - last.getTime()) / (24 * 60 * 60 * 1000));
                log.info("仓库:" + sid + ", 当前时间：" + dateFormat.format(today) + ", 最后库存时间:" + dateFormat.format(last) + ", 间隔天数：" + span);
                if (span <= 0) {
                    return RestResult.fail("没有需要计算的库存");
                } else {
                    span = stockday;
                }
                for (int i = 0; i < span; i++, last = dateUtil.addOneDay(last, 1)) {
                    log.info("开始计算库存时间:" + dateFormat.format(last));
                    RestResult ret = countStockOneDay(gid, sid, last, comms, halfs, oris, stans, dests);
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
        }
        return RestResult.ok();
    }

    public void delStock(int sid, Date date) {
        stockCommodityDayRepository.delete(sid, date);
        stockHalfgoodDayRepository.delete(sid, date);
        stockOriginalDayRepository.delete(sid, date);
        stockStandardDayRepository.delete(sid, date);
        stockDestroyDayRepository.delete(sid, date);
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
        TStockCommodityDay commodity = stockCommodityDayRepository.findLast(sid, 0);
        if (null != commodity) {
            last = commodity.getCdate();
        }
        TStockHalfgoodDay halfgood = stockHalfgoodDayRepository.findLast(sid, 0);
        if (null != halfgood) {
            if (null == last) {
                last = halfgood.getCdate();
            } else {
                if (halfgood.getCdate().after(last)) {
                    last = halfgood.getCdate();
                }
            }
        }
        TStockOriginalDay original = stockOriginalDayRepository.findLast(sid, 0);
        if (null != original) {
            if (null == last) {
                last = original.getCdate();
            } else {
                if (original.getCdate().after(last)) {
                    last = original.getCdate();
                }
            }
        }
        TStockStandardDay standard = stockStandardDayRepository.findLast(sid, 0);
        if (null != standard) {
            if (null == last) {
                last = standard.getCdate();
            } else {
                if (standard.getCdate().after(last)) {
                    last = standard.getCdate();
                }
            }
        }
        TStockDestroyDay destroy = stockDestroyDayRepository.findLast(sid, 0);
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
    private RestResult countStockOneDay(int gid, int sid, Date date, HashMap<Integer, TStockCommodityDay> comms,
                                        HashMap<Integer, TStockHalfgoodDay> halfs, HashMap<Integer, TStockOriginalDay> oris,
                                        HashMap<Integer, TStockStandardDay> stans, HashMap<Integer, TStockDestroyDay> dests) {
        Date start = dateUtil.getStartTime(date);
        Date end = dateUtil.getEndTime(date);
        val agreementOrderCommodities = agreementOrderCommodityRepository.findBySid(sid, start, end);
        log.info("履约订单数:" + agreementOrderCommodities.size());
        handleCommoditys(gid, sid, agreementOrderCommodities, comms, halfs, oris, stans, dests);
        val productOrderCommodities = productOrderCommodityRepository.findBySid(sid, start, end);
        log.info("生产订单数:" + productOrderCommodities.size());
        handleCommoditys(gid, sid, productOrderCommodities, comms, halfs, oris, stans, dests);
        val storageOrderCommodities = storageOrderCommodityRepository.findBySid(sid, start, end);
        log.info("仓储订单数:" + storageOrderCommodities.size());
        handleCommoditys(gid, sid, storageOrderCommodities, comms, halfs, oris, stans, dests);

        // 插入数量大于0的数据，所有数据按start时间算
        start = dateUtil.addOneDay(start, 1);
        for (Map.Entry<Integer, TStockCommodityDay> entry : comms.entrySet()) {
            TStockCommodityDay commodity = entry.getValue();
            log.info("商品：" + commodity.getCid() + ", 数量:" + commodity.getValue() + ", 价格:" + commodity.getPrice());
            commodity.setId(0);
            commodity.setCdate(start);
            if (!stockCommodityDayRepository.insert(commodity)) {
                SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                return RestResult.fail("添加商品" + simpleDateFormat.format(commodity.getCdate()) + "库存记录失败" + commodity.getCid());
            }
        }
        for (Map.Entry<Integer, TStockHalfgoodDay> entry : halfs.entrySet()) {
            TStockHalfgoodDay halfgood = entry.getValue();
            log.info("半成品：" + halfgood.getHid() + ", 数量:" + halfgood.getValue() + ", 价格:" + halfgood.getPrice());
            halfgood.setId(0);
            halfgood.setCdate(start);
            if (!stockHalfgoodDayRepository.insert(halfgood)) {
                SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                return RestResult.fail("添加半成品" + simpleDateFormat.format(halfgood.getCdate()) + "库存记录失败" + halfgood.getHid());
            }
        }
        for (Map.Entry<Integer, TStockOriginalDay> entry : oris.entrySet()) {
            TStockOriginalDay original = entry.getValue();
            log.info("原料：" + original.getOid() + ", 数量:" + original.getValue() + ", 价格:" + original.getPrice());
            original.setId(0);
            original.setCdate(start);
            if (!stockOriginalDayRepository.insert(original)) {
                SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                return RestResult.fail("添加原料" + simpleDateFormat.format(original.getCdate()) + "库存记录失败" + original.getOid());
            }
        }
        for (Map.Entry<Integer, TStockStandardDay> entry : stans.entrySet()) {
            TStockStandardDay standard = entry.getValue();
            log.info("标品：" + standard.getStid() + ", 数量:" + standard.getValue() + ", 价格:" + standard.getPrice());
            standard.setId(0);
            standard.setCdate(start);
            if (!stockStandardDayRepository.insert(standard)) {
                SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                return RestResult.fail("添加标品" + simpleDateFormat.format(standard.getCdate()) + "库存记录失败" + standard.getSid());
            }
        }
        for (Map.Entry<Integer, TStockDestroyDay> entry : dests.entrySet()) {
            TStockDestroyDay destroy = entry.getValue();
            log.info("废料：" + destroy.getDid() + ", 数量:" + destroy.getValue() + ", 价格:" + destroy.getPrice());
            destroy.setId(0);
            destroy.setCdate(start);
            if (!stockDestroyDayRepository.insert(destroy)) {
                SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
                return RestResult.fail("添加废料" + simpleDateFormat.format(destroy.getCdate()) + "库存记录失败" + destroy.getDid());
            }
        }
        return null;
    }

    private void handleCommoditys(int gid, int sid, List<MyOrderCommodity> commodities, HashMap<Integer, TStockCommodityDay> comms,
                                  HashMap<Integer, TStockHalfgoodDay> halfs, HashMap<Integer, TStockOriginalDay> oris,
                                  HashMap<Integer, TStockStandardDay> stans, HashMap<Integer, TStockDestroyDay> dests) {
        for (MyOrderCommodity commodity : commodities) {
            switch (CommodityType.valueOf(commodity.getCtype())) {
                case COMMODITY: {
                    TStockCommodityDay stockCommodity = comms.get(commodity.getCid());
                    if (null == stockCommodity) {
                        // 没数据就先尝试从库存获取
                        stockCommodity = stockCommodityDayRepository.findLast(sid, commodity.getCid());
                        if (null == stockCommodity) {
                            stockCommodity = new TStockCommodityDay();
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
                    stockCommodity.setGid(gid);
                    stockCommodity.setSid(sid);
                    stockCommodity.setCid(commodity.getCid());
                    stockCommodity.setUnit(commodity.getUnit());
                    stockCommodity.setPrice(commodity.getPrice());
                    break;
                }
                case HALFGOOD: {
                    TStockHalfgoodDay stockHalfgood = halfs.get(commodity.getCid());
                    if (null == stockHalfgood) {
                        // 没数据就先尝试从库存获取
                        stockHalfgood = stockHalfgoodDayRepository.findLast(sid, commodity.getCid());
                        if (null == stockHalfgood) {
                            stockHalfgood = new TStockHalfgoodDay();
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
                    stockHalfgood.setGid(gid);
                    stockHalfgood.setSid(sid);
                    stockHalfgood.setHid(commodity.getCid());
                    stockHalfgood.setUnit(commodity.getUnit());
                    stockHalfgood.setPrice(commodity.getPrice());
                    break;
                }
                case ORIGINAL: {
                    TStockOriginalDay stockOriginal = oris.get(commodity.getCid());
                    if (null == stockOriginal) {
                        // 没数据就先尝试从库存获取
                        stockOriginal = stockOriginalDayRepository.findLast(sid, commodity.getCid());
                        if (null == stockOriginal) {
                            stockOriginal = new TStockOriginalDay();
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
                    stockOriginal.setGid(gid);
                    stockOriginal.setSid(sid);
                    stockOriginal.setOid(commodity.getCid());
                    stockOriginal.setUnit(commodity.getUnit());
                    stockOriginal.setPrice(commodity.getPrice());
                    break;
                }
                case STANDARD: {
                    TStockStandardDay stockStandard = stans.get(commodity.getCid());
                    if (null == stockStandard) {
                        // 没数据就先尝试从库存获取
                        stockStandard = stockStandardDayRepository.findLast(sid, commodity.getCid());
                        if (null == stockStandard) {
                            stockStandard = new TStockStandardDay();
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
                    stockStandard.setGid(gid);
                    stockStandard.setSid(sid);
                    stockStandard.setStid(commodity.getCid());
                    stockStandard.setUnit(commodity.getUnit());
                    stockStandard.setPrice(commodity.getPrice());
                    break;
                }
                case DESTROY: {
                    TStockDestroyDay stockDestroy = dests.get(commodity.getCid());
                    if (null == stockDestroy) {
                        // 没数据就先尝试从库存获取
                        stockDestroy = stockDestroyDayRepository.findLast(sid, commodity.getCid());
                        if (null == stockDestroy) {
                            stockDestroy = new TStockDestroyDay();
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
                    stockDestroy.setGid(gid);
                    stockDestroy.setSid(sid);
                    stockDestroy.setDid(commodity.getCid());
                    stockDestroy.setUnit(commodity.getUnit());
                    stockDestroy.setPrice(commodity.getPrice());
                    break;
                }
            }
        }
    }
}