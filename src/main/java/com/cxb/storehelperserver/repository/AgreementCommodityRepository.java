package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementCommodityMapper;
import com.cxb.storehelperserver.model.TAgreementCommodity;
import com.cxb.storehelperserver.model.TAgreementCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyAgreementCommodityMapper;
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
public class AgreementCommodityRepository extends BaseRepository<List> {
    @Resource
    private TAgreementCommodityMapper agreementCommodityMapper;

    @Resource
    private MyAgreementCommodityMapper myAgreementCommodityMapper;

    public AgreementCommodityRepository() {
        init("agreeComm::");
    }

    public List<TAgreementCommodity> find(int oid) {
        List<TAgreementCommodity> agreementCommoditys = getCache(oid, List.class);
        if (null != agreementCommoditys) {
            return agreementCommoditys;
        }

        // 缓存没有就查询数据库
        TAgreementCommodityExample example = new TAgreementCommodityExample();
        example.or().andOidEqualTo(oid);
        agreementCommoditys = agreementCommodityMapper.selectByExample(example);
        if (null != agreementCommoditys) {
            setCache(oid, agreementCommoditys);
        }
        return agreementCommoditys;
    }

    public List<MyOrderCommodity> findByGid(int gid, Date start, Date end) {
        return myAgreementCommodityMapper.selectByGid(gid, start, end);
    }

    public List<MyOrderCommodity> findBySid(int sid, Date start, Date end) {
        return myAgreementCommodityMapper.selectBySid(sid, start, end);
    }

    // 注意：数据被缓存在AgreementCommodityService，所以不能直接调用该函数
    public boolean update(List<TAgreementCommodity> rows, int oid) {
        delete(oid);
        for (TAgreementCommodity agreementCommodity : rows) {
            if (agreementCommodityMapper.insert(agreementCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TAgreementCommodityExample example = new TAgreementCommodityExample();
        example.or().andOidEqualTo(oid);
        return agreementCommodityMapper.deleteByExample(example) > 0;
    }
}
