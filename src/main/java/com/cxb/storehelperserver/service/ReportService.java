package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.*;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.cxb.storehelperserver.util.Permission.dashboard_report;
import static com.cxb.storehelperserver.util.Permission.mp_report;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;
import static com.cxb.storehelperserver.util.TypeDefine.CommodityType.*;
import static com.cxb.storehelperserver.util.TypeDefine.ReportCycleType;
import static com.cxb.storehelperserver.util.TypeDefine.ReportCycleType.*;

/**
 * desc: 报表业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ReportService {
    @Resource
    private CheckService checkService;

    @Resource
    private StockService stockService;

    @Resource
    private MarketCommodityDetailRepository marketCommodityDetailRepository;

    @Resource
    private MarketStandardDetailRepository marketStandardDetailRepository;

    @Resource
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

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
    private StorageRepository storageRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult getTodayReport(int id, int gid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 权限校验，必须admin
        if (!checkService.checkRolePermission(id, dashboard_report) && !checkService.checkRolePermissionMp(id, mp_report)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 使用昨天时间
        Date today = new Date();
        Date yesterday = dateUtil.addOneDay(today, -1);
        Date start = dateUtil.getStartTime(yesterday);
        Date end = dateUtil.getEndTime(yesterday);

        // TODO 销售报表
        val data = new HashMap<String, Object>();
        val market = new HashMap<String, Object>();
        data.put("market", market);
        market.put("total", 0);
        market.put("list", null);

        // 仓储订单
        val storage = new HashMap<String, Object>();
        data.put("storage", storage);
        val storageOrders = storageOrderRepository.getAllByDate(gid, start, end);
        storage.put("total", (null == storageOrders || storageOrders.isEmpty()) ? 0 : storageOrders.size());
        storage.put("list", storageOrders);

        // 履约订单
        val agreement = new HashMap<String, Object>();
        data.put("agreement", agreement);
        val agreementOrders = agreementOrderRepository.getAllByDate(gid, start, end);
        agreement.put("total", (null == agreementOrders || agreementOrders.isEmpty()) ? 0 : agreementOrders.size());
        agreement.put("list", agreementOrders);

        // 生产订单
        val product = new HashMap<String, Object>();
        data.put("product", product);
        val productOrders = productOrderRepository.getAllByDate(gid, start, end);
        product.put("total", (null == productOrders || productOrders.isEmpty()) ? 0 : productOrders.size());
        product.put("list", productOrders);

        // 库存
        val cdata = new HashMap<Integer, Integer>();
        val commodities = stockService.getAllStockCommodity(gid, 0, today, REPORT_DAILY);
        if (null != commodities && !commodities.isEmpty()) {
            for (MyStockCommodity c : commodities) {
                cdata.merge(c.getSid(), c.getValue(), Integer::sum);
            }
        }
        val hdata = new HashMap<Integer, Integer>();
        val halfgoods = stockService.getAllStockHalfgood(gid, 0, today, REPORT_DAILY);
        if (null != halfgoods && !halfgoods.isEmpty()) {
            for (MyStockHalfgood c : halfgoods) {
                hdata.merge(c.getSid(), c.getValue(), Integer::sum);
            }
        }
        val odata = new HashMap<Integer, Integer>();
        val originals = stockService.getAllStockOriginal(gid, 0, today, REPORT_DAILY);
        if (null != originals && !originals.isEmpty()) {
            for (MyStockOriginal c : originals) {
                odata.merge(c.getSid(), c.getValue(), Integer::sum);
            }
        }
        val sdata = new HashMap<Integer, Integer>();
        val standards = stockService.getAllStockStandard(gid, 0, today, REPORT_DAILY);
        if (null != standards && !standards.isEmpty()) {
            for (MyStockStandard c : standards) {
                sdata.merge(c.getSid(), c.getValue(), Integer::sum);
            }
        }
        val ddata = new HashMap<Integer, Integer>();
        val destroys = stockService.getAllStockDestroy(gid, 0, today, REPORT_DAILY);
        if (null != destroys && !destroys.isEmpty()) {
            for (MyStockDestroy c : destroys) {
                ddata.merge(c.getSid(), c.getValue(), Integer::sum);
            }
        }

        // 仓库信息
        val stocks = new ArrayList<>();
        int total = storageRepository.total(gid, null);
        val storages = storageRepository.pagination(gid, 1, total, null);
        if (null != storages && !storages.isEmpty()) {
            for (TStorage s : storages) {
                int sid = s.getId();
                int sum = 0;
                for (Map.Entry<Integer, Integer> entry : cdata.entrySet()) {
                    if (entry.getKey().equals(sid)) {
                        sum += entry.getValue();
                    }
                }
                for (Map.Entry<Integer, Integer> entry : hdata.entrySet()) {
                    if (entry.getKey().equals(sid)) {
                        sum += entry.getValue();
                    }
                }
                for (Map.Entry<Integer, Integer> entry : odata.entrySet()) {
                    if (entry.getKey().equals(sid)) {
                        sum += entry.getValue();
                    }
                }
                for (Map.Entry<Integer, Integer> entry : sdata.entrySet()) {
                    if (entry.getKey().equals(sid)) {
                        sum += entry.getValue();
                    }
                }
                for (Map.Entry<Integer, Integer> entry : ddata.entrySet()) {
                    if (entry.getKey().equals(sid)) {
                        sum += entry.getValue();
                    }
                }
                val stock = new HashMap<String, Object>();
                stock.put("name", s.getName());
                stock.put("total", sum);
                stocks.add(stock);
            }
        }
        data.put("stock", stocks);
        return RestResult.ok(data);
    }

    public RestResult getMarketReport(int id, int gid, int mid, int type, ReportCycleType cycle) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        SimpleDateFormat dateFormat = dateUtil.getSimpleDateFormat();
        Date end = dateUtil.getEndTime(new Date());
        Date start = dateUtil.addOneDay(end, -7);
        List<MyMarketReport> list = null;
        if (COMMODITY.getValue() == type) {
            list = marketCommodityDetailRepository.findByDate(gid, mid, start, end);
        } else if (STANDARD.getValue() == type) {
            list = marketStandardDetailRepository.findByDate(gid, mid, start, end);
        } else {
            list = marketCommodityDetailRepository.findByDate(gid, mid, start, end);
            if (null == list || list.isEmpty()) {
                list = marketStandardDetailRepository.findByDate(gid, mid, start, end);
            } else {
                val list2 = marketStandardDetailRepository.findByDate(gid, mid, start, end);
                if (null != list2 && !list2.isEmpty()) {
                    list.addAll(list2);
                }
            }
        }
        val list2 = new ArrayList<>();
        for (MyMarketReport detail : list) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", detail.getId());
            tmp.put("type", detail.getType());
            tmp.put("cid", detail.getCid());
            tmp.put("value", detail.getValue());
            tmp.put("price", detail.getPrice());
            tmp.put("total", detail.getTotal());
            tmp.put("date", dateFormat.format(detail.getCdate()));
            list2.add(tmp);
        }
        val data = new HashMap<String, Object>();
        data.put("list", list2);
        return RestResult.ok(data);
    }

    public RestResult getAgreementReport(int id, int gid, int sid, ReportCycleType cycle) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        SimpleDateFormat dateFormat = dateUtil.getSimpleDateFormat();
        Date end = dateUtil.getEndTime(new Date());
        Date start = dateUtil.addOneDay(end, -7);
        val orders = userOrderCompleteRepository.findByAgreement(gid, sid, start, end);
        val list = new ArrayList<>();
        for (MyUserOrderComplete order : orders) {
            val tmp = new HashMap<String, Object>();
            tmp.put("num", order.getCnum());
            tmp.put("total", order.getCtotal());
            tmp.put("type", order.getOtype());
            tmp.put("date", dateFormat.format(order.getCdate()));
            list.add(tmp);
        }
        val data = new HashMap<String, Object>();
        data.put("list", list);
        return RestResult.ok(data);
    }

    public RestResult getProductReport(int id, int gid, int sid, ReportCycleType cycle) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        SimpleDateFormat dateFormat = dateUtil.getSimpleDateFormat();
        Date end = dateUtil.getEndTime(new Date());
        Date start = dateUtil.addOneDay(end, -7);
        val orders = userOrderCompleteRepository.findByProduct(gid, sid, start, end);
        val list = new ArrayList<>();
        for (MyUserOrderComplete order : orders) {
            val tmp = new HashMap<String, Object>();
            tmp.put("num", order.getCnum());
            tmp.put("total", order.getCtotal());
            tmp.put("type", order.getOtype());
            tmp.put("date", dateFormat.format(order.getCdate()));
            list.add(tmp);
        }
        val data = new HashMap<String, Object>();
        data.put("list", list);
        return RestResult.ok(data);
    }

    public RestResult getStorageReport(int id, int gid, int sid, ReportCycleType cycle) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        SimpleDateFormat dateFormat = dateUtil.getSimpleDateFormat();
        Date end = dateUtil.getEndTime(new Date());
        Date start = dateUtil.addOneDay(end, -7);
        val orders = userOrderCompleteRepository.findByStorage(gid, sid, start, end);
        val list = new ArrayList<>();
        for (MyUserOrderComplete order : orders) {
            val tmp = new HashMap<String, Object>();
            tmp.put("num", order.getCnum());
            tmp.put("total", order.getCtotal());
            tmp.put("type", order.getOtype());
            tmp.put("date", dateFormat.format(order.getCdate()));
            list.add(tmp);
        }
        val data = new HashMap<String, Object>();
        data.put("list", list);
        return RestResult.ok(data);
    }

    public RestResult getStockReport(int id, int gid, CommodityType type, ReportCycleType cycle) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }

        SimpleDateFormat dateFormat = dateUtil.getSimpleDateFormat();
        Date end = dateUtil.getEndTime(new Date());
        Date start = dateUtil.addOneDay(end, -7);
        List<MyStockReport> stocks = null;
        switch (type) {
            case COMMODITY:
                stocks = stockCommodityDayRepository.findReport(gid, start, end);
                break;
            case HALFGOOD:
                stocks = stockHalfgoodDayRepository.findReport(gid, start, end);
                break;
            case ORIGINAL:
                stocks = stockOriginalDayRepository.findReport(gid, start, end);
                break;
            case STANDARD:
                stocks = stockStandardDayRepository.findReport(gid, start, end);
                break;
            default:
                stocks = stockDestroyDayRepository.findReport(gid, start, end);
                break;
        }
        val list = new ArrayList<>();
        for (MyStockReport stock : stocks) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", stock.getId());
            tmp.put("total", stock.getTotal());
            tmp.put("date", dateFormat.format(stock.getCdate()));
            list.add(tmp);
        }
        val data = new HashMap<String, Object>();
        data.put("list", list);
        return RestResult.ok(data);
    }
}
