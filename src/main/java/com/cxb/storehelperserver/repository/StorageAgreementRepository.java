package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageAgreementMapper;
import com.cxb.storehelperserver.model.TStorageAgreement;
import com.cxb.storehelperserver.model.TStorageAgreementExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 入库与履约关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StorageAgreementRepository extends BaseRepository<TStorageAgreement> {
    @Resource
    private TStorageAgreementMapper storageAgreementMapper;

    public StorageAgreementRepository() {
        init("storAgreement::");
    }

    public TStorageAgreement find(int oid) {
        TStorageAgreement storageAgreement = getCache(oid, TStorageAgreement.class);
        if (null != storageAgreement) {
            return storageAgreement;
        }

        // 缓存没有就查询数据库
        TStorageAgreementExample example = new TStorageAgreementExample();
        example.or().andOidEqualTo(oid);
        storageAgreement = storageAgreementMapper.selectOneByExample(example);
        if (null != storageAgreement) {
            setCache(oid, storageAgreement);
        }
        return storageAgreement;
    }

    public List<TStorageAgreement> findByDid(int aid) {
        TStorageAgreementExample example = new TStorageAgreementExample();
        example.or().andAidEqualTo(aid);
        return storageAgreementMapper.selectByExample(example);
    }

    public boolean checkByDid(int aid) {
        TStorageAgreementExample example = new TStorageAgreementExample();
        example.or().andAidEqualTo(aid);
        return null != storageAgreementMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int aid) {
        TStorageAgreement row = new TStorageAgreement();
        row.setOid(oid);
        row.setAid(aid);
        if (storageAgreementMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageAgreement row) {
        if (storageAgreementMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int aid) {
        delCache(oid);
        TStorageAgreementExample example = new TStorageAgreementExample();
        example.or().andOidEqualTo(oid).andAidEqualTo(aid);
        return storageAgreementMapper.deleteByExample(example) > 0;
    }
}
