package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityMapper;
import com.cxb.storehelperserver.model.TCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * desc: 市场仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class MarketRepository extends BaseRepository<TCommodity> {
    //@Resource
    private TCommodityMapper commodityMapper;

    public MarketRepository() {
        init("market::");
    }

    public TCommodity find() {
        return null;
    }
}
