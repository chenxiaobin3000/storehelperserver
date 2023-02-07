package com.cxb.storehelperserver.handle;

import com.cxb.storehelperserver.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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

    @Scheduled(cron = "1 0 0 * * ?")
    private void scheduledStock() {
        log.info("scheduled stock");
        stockService.countStock(1, 2);
    }
}
