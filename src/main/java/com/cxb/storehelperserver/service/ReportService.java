package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TAgreementOrder;
import com.cxb.storehelperserver.model.TProductOrder;
import com.cxb.storehelperserver.model.TStorageOrder;
import com.cxb.storehelperserver.repository.AgreementOrderRepository;
import com.cxb.storehelperserver.repository.ProductOrderRepository;
import com.cxb.storehelperserver.repository.StorageOrderRepository;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;

import static com.cxb.storehelperserver.util.Permission.dashboard_report;
import static com.cxb.storehelperserver.util.Permission.mp_report;

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
    private AgreementOrderRepository agreementOrderRepository;

    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private StorageOrderRepository storageOrderRepository;

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
        Date yesterday = dateUtil.addOneDay(new Date(), -1);
        Date start = dateUtil.getStartTime(yesterday);
        Date end = dateUtil.getEndTime(yesterday);


        // TODO 销售报表
        val data = new HashMap<String, Object>();

        // 仓储订单
        // 根据当天时间计算
        val storageOrders = storageOrderRepository.getAllByDate(gid, start, end);
        if (null != storageOrders && !storageOrders.isEmpty()) {
            for (TStorageOrder order : storageOrders) {

            }
        }

        // 履约订单
        total = agreementOrderRepository.total(gid, null);
        val agreementOrders = agreementOrderRepository.pagination(gid, 1, total, null);
        if (null != agreementOrders && !agreementOrders.isEmpty()) {
            for (TAgreementOrder order : agreementOrders) {

            }
        }

        // 生产订单
        total = productOrderRepository.total(gid, null);
        val productOrders = productOrderRepository.pagination(gid, 1, total, null);
        if (null != productOrders && !productOrders.isEmpty()) {
            for (TProductOrder order : productOrders) {

            }
        }

        // 库存
        //stockService.getStockCommodity(id, 0, );
        return RestResult.ok(data);
    }
}
