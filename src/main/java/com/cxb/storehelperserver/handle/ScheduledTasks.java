package com.cxb.storehelperserver.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * desc:
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Component
public class ScheduledTasks {
    @Scheduled(cron = "1 0 0 * * ?")
    private void scheduledStock() {
        log.info("scheduled stock");
    }
}
