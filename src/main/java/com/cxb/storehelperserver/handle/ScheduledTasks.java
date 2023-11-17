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

    // 00:01:00执行
    @Scheduled(cron = "0 1 0 * * ?")
    private void scheduledStock() {
        log.info("scheduled stock");
    }
}
