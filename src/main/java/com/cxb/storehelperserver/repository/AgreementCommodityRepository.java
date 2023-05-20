package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementCommodityMapper;
import com.cxb.storehelperserver.model.TAgreementCommodity;
import com.cxb.storehelperserver.model.TAgreementCommodityExample;
import com.cxb.storehelperserver.model.TAgreementOrder;
import com.cxb.storehelperserver.repository.mapper.MyCommodityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

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
    private MyCommodityMapper myCommodityMapper;

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

    public int total(int gid, int aid, int type, ReviewType review, CompleteType complete, Date start, Date end, List<Integer> ids) {
        return myCommodityMapper.countByAgreement(gid, aid, type, review.getValue(), complete.getValue(), start, end, ids);
    }

    public List<TAgreementOrder> pagination(int gid, int aid, int type, int page, int limit, ReviewType review, CompleteType complete, Date start, Date end, List<Integer> ids) {
        return myCommodityMapper.paginationByAgreement(gid, aid, type, (page - 1) * limit, limit, review.getValue(), complete.getValue(), start, end, ids);
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
