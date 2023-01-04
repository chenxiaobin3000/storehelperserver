package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.model.TCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * desc: 履约仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class AgreementRepository extends BaseRepository<TCategory> {
    //@Resource
    //private TAgreementMapper agreementMapper;

    public AgreementRepository() {
        init("agree::");
    }

    public TCategory find() {
        return null;
    }
}
