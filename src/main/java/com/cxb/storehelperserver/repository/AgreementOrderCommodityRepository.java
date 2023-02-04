package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementOrderCommodityMapper;
import com.cxb.storehelperserver.model.TAgreementOrderCommodity;
import com.cxb.storehelperserver.model.TAgreementOrderCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyAgreementOrderCommodityMapper;
import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 履约出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class AgreementOrderCommodityRepository extends BaseRepository<List> {
    @Resource
    private TAgreementOrderCommodityMapper agreementOrderCommodityMapper;

    @Resource
    private MyAgreementOrderCommodityMapper myAgreementOrderCommodityMapper;

    public AgreementOrderCommodityRepository() {
        init("aoComm::");
    }

    public List<TAgreementOrderCommodity> find(int oid) {
        List<TAgreementOrderCommodity> agreementOrderCommoditys = getCache(oid, List.class);
        if (null != agreementOrderCommoditys) {
            return agreementOrderCommoditys;
        }

        // 缓存没有就查询数据库
        TAgreementOrderCommodityExample example = new TAgreementOrderCommodityExample();
        example.or().andOidEqualTo(oid);
        agreementOrderCommoditys = agreementOrderCommodityMapper.selectByExample(example);
        if (null != agreementOrderCommoditys) {
            setCache(oid, agreementOrderCommoditys);
        }
        return agreementOrderCommoditys;
    }

    public List<MyOrderCommodity> findByGid(int gid, Date start, Date end) {
        return myAgreementOrderCommodityMapper.selectByGid(gid, start, end);
    }

    public List<MyOrderCommodity> findBySid(int sid, Date start, Date end) {
        return myAgreementOrderCommodityMapper.selectBySid(sid, start, end);
    }

    // 注意：数据被缓存在AgreementCommodityService，所以不能直接调用该函数
    public boolean update(List<TAgreementOrderCommodity> rows, int oid) {
        delete(oid);
        for (TAgreementOrderCommodity agreementOrderCommodity : rows) {
            if (agreementOrderCommodityMapper.insert(agreementOrderCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TAgreementOrderCommodityExample example = new TAgreementOrderCommodityExample();
        example.or().andOidEqualTo(oid);
        return agreementOrderCommodityMapper.deleteByExample(example) > 0;
    }
}
