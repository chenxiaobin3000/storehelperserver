package com.cxb.storehelperserver.util;

import com.cxb.storehelperserver.model.TSoInCommodity;
import com.cxb.storehelperserver.model.TSoInOrder;
import com.cxb.storehelperserver.repository.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * desc: 仓库订单业务
 * auth: cxb
 * date: 2023/1/14
 */
public class StorageOrder extends StorageCache {
    private ScInCommodityRepository scInCommodityRepository;
    private ScInOrderRepository scInOrderRepository;
    private ScOutCommodityRepository scOutCommodityRepository;
    private ScOutOrderRepository scOutOrderRepository;

    private SdInCommodityRepository sdInCommodityRepository;
    private SdInOrderRepository sdInOrderRepository;
    private SdOutCommodityRepository sdOutCommodityRepository;
    private SdOutOrderRepository sdOutOrderRepository;

    private ShInCommodityRepository shInCommodityRepository;
    private ShInOrderRepository shInOrderRepository;
    private ShOutCommodityRepository shOutCommodityRepository;
    private ShOutOrderRepository shOutOrderRepository;

    private SoInCommodityRepository soInCommodityRepository;
    private SoInOrderRepository soInOrderRepository;
    private SoOutCommodityRepository soOutCommodityRepository;
    private SoOutOrderRepository soOutOrderRepository;

    private SsInCommodityRepository ssInCommodityRepository;
    private SsInOrderRepository ssInOrderRepository;
    private SsOutCommodityRepository ssOutCommodityRepository;
    private SsOutOrderRepository ssOutOrderRepository;

    /**
     * desc: 初始化数据库
     */
    protected void init(RedisTemplate<String, Object> redisTemplate,
                        ScInCommodityRepository scInCommodityRepository, ScInOrderRepository scInOrderRepository,
                        ScOutCommodityRepository scOutCommodityRepository, ScOutOrderRepository scOutOrderRepository,
                        SdInCommodityRepository sdInCommodityRepository, SdInOrderRepository sdInOrderRepository,
                        SdOutCommodityRepository sdOutCommodityRepository, SdOutOrderRepository sdOutOrderRepository,
                        ShInCommodityRepository shInCommodityRepository, ShInOrderRepository shInOrderRepository,
                        ShOutCommodityRepository shOutCommodityRepository, ShOutOrderRepository shOutOrderRepository,
                        SoInCommodityRepository soInCommodityRepository, SoInOrderRepository soInOrderRepository,
                        SoOutCommodityRepository soOutCommodityRepository, SoOutOrderRepository soOutOrderRepository,
                        SsInCommodityRepository ssInCommodityRepository, SsInOrderRepository ssInOrderRepository,
                        SsOutCommodityRepository ssOutCommodityRepository, SsOutOrderRepository ssOutOrderRepository) {
        super.init(redisTemplate);
        this.scInCommodityRepository = scInCommodityRepository;
        this.scInOrderRepository = scInOrderRepository;
        this.scOutCommodityRepository = scOutCommodityRepository;
        this.scOutOrderRepository = scOutOrderRepository;

        this.sdInCommodityRepository = sdInCommodityRepository;
        this.sdInOrderRepository = sdInOrderRepository;
        this.sdOutCommodityRepository = sdOutCommodityRepository;
        this.sdOutOrderRepository = sdOutOrderRepository;

        this.shInCommodityRepository = shInCommodityRepository;
        this.shInOrderRepository = shInOrderRepository;
        this.shOutCommodityRepository = shOutCommodityRepository;
        this.shOutOrderRepository = shOutOrderRepository;

        this.soInCommodityRepository = soInCommodityRepository;
        this.soInOrderRepository = soInOrderRepository;
        this.soOutCommodityRepository = soOutCommodityRepository;
        this.soOutOrderRepository = soOutOrderRepository;

        this.ssInCommodityRepository = ssInCommodityRepository;
        this.ssInOrderRepository = ssInOrderRepository;
        this.ssOutCommodityRepository = ssOutCommodityRepository;
        this.ssOutOrderRepository = ssOutOrderRepository;
    }

    protected int addOriginalIn(TSoInOrder order, List<TSoInCommodity> commodities) {
        if (!this.soInOrderRepository.insert(order)) {
            return 0;
        }
        int id = order.getId();
        for (TSoInCommodity c : commodities) {
            c.setOrder(id);
            if (!this.soInCommodityRepository.insert(c)) {
                return 0;
            }
        }
        return id;
    }
}
