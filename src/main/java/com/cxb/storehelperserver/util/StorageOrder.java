package com.cxb.storehelperserver.util;

import com.cxb.storehelperserver.model.TSoInCommodity;
import com.cxb.storehelperserver.model.TSoInOrder;
import com.cxb.storehelperserver.repository.*;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓库订单业务
 * auth: cxb
 * date: 2023/1/14
 */
public class StorageOrder extends StorageCache {
    @Resource
    private ScInCommodityRepository scInCommodityRepository;
    @Resource
    private ScInOrderRepository scInOrderRepository;
    @Resource
    private ScOutCommodityRepository scOutCommodityRepository;
    @Resource
    private ScOutOrderRepository scOutOrderRepository;

    @Resource
    private SdInCommodityRepository sdInCommodityRepository;
    @Resource
    private SdInOrderRepository sdInOrderRepository;
    @Resource
    private SdOutCommodityRepository sdOutCommodityRepository;
    @Resource
    private SdOutOrderRepository sdOutOrderRepository;

    @Resource
    private ShInCommodityRepository shInCommodityRepository;
    @Resource
    private ShInOrderRepository shInOrderRepository;
    @Resource
    private ShOutCommodityRepository shOutCommodityRepository;
    @Resource
    private ShOutOrderRepository shOutOrderRepository;

    @Resource
    private SoInCommodityRepository soInCommodityRepository;
    @Resource
    private SoInOrderRepository soInOrderRepository;
    @Resource
    private SoOutCommodityRepository soOutCommodityRepository;
    @Resource
    private SoOutOrderRepository soOutOrderRepository;

    @Resource
    private SsInCommodityRepository ssInCommodityRepository;
    @Resource
    private SsInOrderRepository ssInOrderRepository;
    @Resource
    private SsOutCommodityRepository ssOutCommodityRepository;
    @Resource
    private SsOutOrderRepository ssOutOrderRepository;

    protected int addOriginalIn(TSoInOrder order, List<TSoInCommodity> commodities) {
        if (!soInOrderRepository.insert(order)) {
            return 0;
        }
        int id = order.getId();
        for (TSoInCommodity c : commodities) {
            c.setOrid(id);
            if (!soInCommodityRepository.insert(c)) {
                return 0;
            }
        }
        return id;
    }
}
