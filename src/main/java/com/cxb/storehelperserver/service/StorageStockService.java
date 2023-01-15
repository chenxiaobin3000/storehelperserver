package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TSoInCommodity;
import com.cxb.storehelperserver.model.TSoInOrder;
import com.cxb.storehelperserver.model.TStorageOriginal;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.StorageOrder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 仓库库存业务
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageStockService extends StorageOrder {
    @Resource
    protected RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;
    @Resource
    private StorageOriginalRepository storageOriginalRepository;
    @Resource
    private StorageStandardRepository storageStandardRepository;
    @Resource
    private StorageDestroyRepository storageDestroyRepository;

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

    public StorageStockService() {
        init(redisTemplate,
                scInCommodityRepository, scInOrderRepository, scOutCommodityRepository, scOutOrderRepository,
                sdInCommodityRepository, sdInOrderRepository, sdOutCommodityRepository, sdOutOrderRepository,
                shInCommodityRepository, shInOrderRepository, shOutCommodityRepository, shOutOrderRepository,
                soInCommodityRepository, soInOrderRepository, soOutCommodityRepository, soOutOrderRepository,
                ssInCommodityRepository, ssInOrderRepository, ssOutCommodityRepository, ssOutOrderRepository);
    }

    /*
     * desc: 原料进货
     */
    public RestResult purchaseOriginal(int id, TSoInOrder order, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices) {
        // TODO 校验权限

        // 生成进货单
        int size = commoditys.size();
        if (size != values.size() || size != prices.size()) {
            return RestResult.fail("商品信息出错");
        }
        int total = 0;
        val list = new ArrayList<TSoInCommodity>();
        for (int i = 0; i < size; i++) {
            TSoInCommodity c = new TSoInCommodity();
            c.setOid(commoditys.get(i));
            c.setValue(values.get(i));
            c.setPrice(prices.get(i));
            list.add(c);
            total += values.get(i);
        }
        int orderid = addOriginalIn(order, list);
        if (0 == orderid) {
            return RestResult.fail("生成进货订单失败");
        }

        // 修改库存数量
        TStorageOriginal storageOriginal = new TStorageOriginal();
        storageOriginal.setSid(order.getSid());
        storageOriginal.setOid(orderid);//测试失败0
        storageOriginal.setValue(total);
        if (!storageOriginalRepository.insert(storageOriginal)) {
            // TODO 测试写入失败回滚
            return RestResult.fail("生成进货订单失败");
        }

        // watchCommodityCache();
        // 写入缓存
        multiCache();

        val exec = execCache();

        // 异常就回滚
        // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return RestResult.ok();
    }

    /*
     * desc: 标品进货
     */
    public RestResult purchaseStandard(int id, int sid, int cid, List<Integer> commoditys, List<Integer> values, List<BigDecimal> prices) {
        // 写入数据库
        // TODO 测试写入失败回滚
        // return fail

        // watchCommodityCache();
        // 写入缓存
        multiCache();

        execCache();

        // 异常就回滚
        // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return RestResult.ok();
    }

    /*
     * desc: 生产开始
     */
    public RestResult process(int id, int gid, int sid, List<Integer> commoditys, List<Integer> values, List<String> prices) {
        return RestResult.ok();
    }

    /*
     * desc: 生产完成
     */
    public RestResult complete(int id, int gid, int sid, List<Integer> commoditys, List<Integer> values, List<String> prices) {
        return RestResult.ok();
    }
}
