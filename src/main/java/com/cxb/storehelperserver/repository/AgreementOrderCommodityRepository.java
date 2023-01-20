package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementOrderCommodityMapper;
import com.cxb.storehelperserver.model.TAgreementOrderCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 履约出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class AgreementOrderCommodityRepository extends BaseRepository<TAgreementOrderCommodity> {
    @Resource
    private TAgreementOrderCommodityMapper agreementOrderCommodityMapper;

    public AgreementOrderCommodityRepository() {
        init("aoComm::");
    }

    public TAgreementOrderCommodity find(int id) {
        TAgreementOrderCommodity agreementOrderCommodity = getCache(id, TAgreementOrderCommodity.class);
        if (null != agreementOrderCommodity) {
            return agreementOrderCommodity;
        }

        // 缓存没有就查询数据库
        agreementOrderCommodity = agreementOrderCommodityMapper.selectByPrimaryKey(id);
        if (null != agreementOrderCommodity) {
            setCache(id, agreementOrderCommodity);
        }
        return agreementOrderCommodity;
    }

    public boolean insert(TAgreementOrderCommodity row) {
        if (agreementOrderCommodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementOrderCommodity row) {
        if (agreementOrderCommodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return agreementOrderCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
