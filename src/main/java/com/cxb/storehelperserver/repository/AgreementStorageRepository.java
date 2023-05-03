package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementStorageMapper;
import com.cxb.storehelperserver.model.TAgreementStorage;
import com.cxb.storehelperserver.model.TAgreementStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 履约仓库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class AgreementStorageRepository extends BaseRepository<TAgreementStorage> {
    @Resource
    private TAgreementStorageMapper agreementStorageMapper;

    public AgreementStorageRepository() {
        init("agreeStorage::");
    }

    public TAgreementStorage find(int oid) {
        TAgreementStorage agreementStorage = getCache(oid, TAgreementStorage.class);
        if (null != agreementStorage) {
            return agreementStorage;
        }

        // 缓存没有就查询数据库
        TAgreementStorageExample example = new TAgreementStorageExample();
        example.or().andOidEqualTo(oid);
        agreementStorage = agreementStorageMapper.selectOneByExample(example);
        if (null != agreementStorage) {
            setCache(oid, agreementStorage);
        }
        return agreementStorage;
    }

    public TAgreementStorage findBySid(int sid) {
        TAgreementStorageExample example = new TAgreementStorageExample();
        example.or().andSidEqualTo(sid);
        return agreementStorageMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int sid) {
        TAgreementStorage row = new TAgreementStorage();
        row.setOid(oid);
        row.setSid(sid);
        if (agreementStorageMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementStorage row) {
        if (agreementStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int sid) {
        delCache(oid);
        TAgreementStorageExample example = new TAgreementStorageExample();
        example.or().andOidEqualTo(oid).andSidEqualTo(sid);
        return agreementStorageMapper.deleteByExample(example) > 0;
    }
}
