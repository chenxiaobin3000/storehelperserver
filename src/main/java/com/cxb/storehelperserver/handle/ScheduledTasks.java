package com.cxb.storehelperserver.handle;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.service.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * desc:
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Component
public class ScheduledTasks {
    @Resource
    private StockService stockService;

    @Resource
    private StockCloudService stockCloudService;

    @Resource
    private GroupRepository groupRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private MarketAccountRepository marketAccountRepository;

    // 00:01:00执行
    @Scheduled(cron = "0 1 0 * * ?")
    private void scheduledStock() {
        log.info("scheduled stock");
        Date today = new Date();
        int total = groupRepository.total(null);
        val groups = groupRepository.pagination(1, total, null);
        for (TGroup group : groups) {
            int gid = group.getId();

            // 本地库存
            int total2 = storageRepository.total(gid, null);
            val list2 = storageRepository.pagination(gid, 1, total2, null);
            if (null != list2 && !list2.isEmpty()) {
                for (TStorage s : list2) {
                    stockService.countStockDay(gid, s.getId(), today);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        log.error(e.toString());
                    }
                }
            }

            // 平台库存
            int total3 = marketAccountRepository.total(gid, 0);
            val list3 = marketAccountRepository.pagination(gid, 0, 1, total3);
            if (null != list3 && !list3.isEmpty()) {
                for (TMarketAccount s : list3) {
                    stockCloudService.countStockDay(gid, s.getId(), today);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        log.error(e.toString());
                    }
                }
            }
        }
    }
}
