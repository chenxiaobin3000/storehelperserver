package com.cxb.storehelperserver.handle;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TStorage;
import com.cxb.storehelperserver.repository.GroupRepository;
import com.cxb.storehelperserver.repository.StorageRepository;
import com.cxb.storehelperserver.service.StockService;
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
    private GroupRepository groupRepository;

    @Resource
    private StorageRepository storageRepository;

    // 00:01:00执行
    @Scheduled(cron = "0 1 0 * * ?")
    private void scheduledStock() {
        log.info("scheduled stock");

        // TODO 清7天之前销售数据

        Date today = new Date();
        int total = groupRepository.total(null);
        val groups = groupRepository.pagination(1, total, null);
        for (TGroup group : groups) {
            int gid = group.getId();
            int total2 = storageRepository.total(gid, null);
            val list2 = storageRepository.pagination(gid, 1, total2, null);
            if (null != list2 && !list2.isEmpty()) {
                for (TStorage s : list2) {
                    for (int i = 1; i < 5; i++) {
                        stockService.countStockDay(gid, s.getId(), i, today);
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
}
