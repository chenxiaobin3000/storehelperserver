package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductAgreementMapper;
import com.cxb.storehelperserver.model.TProductAgreement;
import com.cxb.storehelperserver.model.TProductAgreementExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 生产与履约关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class ProductAgreementRepository extends BaseRepository<TProductAgreement> {
    @Resource
    private TProductAgreementMapper productAgreementMapper;

    public ProductAgreementRepository() {
        init("productAgree::");
    }

    public TProductAgreement find(int oid) {
        TProductAgreement productAgreement = getCache(oid, TProductAgreement.class);
        if (null != productAgreement) {
            return productAgreement;
        }

        // 缓存没有就查询数据库
        TProductAgreementExample example = new TProductAgreementExample();
        example.or().andAidEqualTo(oid);
        productAgreement = productAgreementMapper.selectOneByExample(example);
        if (null != productAgreement) {
            setCache(oid, productAgreement);
        }
        return productAgreement;
    }

    public List<TProductAgreement> findByAid(int aid) {
        TProductAgreementExample example = new TProductAgreementExample();
        example.or().andAidEqualTo(aid);
        return productAgreementMapper.selectByExample(example);
    }

    public boolean checkByAid(int aid) {
        TProductAgreementExample example = new TProductAgreementExample();
        example.or().andAidEqualTo(aid);
        return null != productAgreementMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int aid) {
        TProductAgreement row = new TProductAgreement();
        row.setOid(oid);
        row.setAid(aid);
        if (productAgreementMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TProductAgreement row) {
        if (productAgreementMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int aid) {
        delCache(oid);
        TProductAgreementExample example = new TProductAgreementExample();
        example.or().andOidEqualTo(oid).andAidEqualTo(aid);
        return productAgreementMapper.deleteByExample(example) > 0;
    }
}
