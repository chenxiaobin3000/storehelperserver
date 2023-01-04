package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.model.TCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * desc: 充值仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class ChargeRepository extends BaseRepository<TCategory> {
    //@Resource
    //private TChargeMapper chargeMapper;

    public ChargeRepository() {
        init("charge::");
    }

    public TCategory find() {
        return null;
    }
}
