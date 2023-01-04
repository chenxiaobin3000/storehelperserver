package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityMapper;
import com.cxb.storehelperserver.model.TCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * desc: 报表仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class ReportRepository extends BaseRepository<TCommodity> {
    //@Resource
    private TCommodityMapper commodityMapper;

    public ReportRepository() {
        init("report::");
    }

    public TCommodity find() {
        return null;
    }
}
