package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudAgreementMapper;
import com.cxb.storehelperserver.model.TCloudAgreement;
import com.cxb.storehelperserver.model.TCloudAgreementExample;
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
public class CloudAgreementRepository extends BaseRepository<TCloudAgreement> {
    @Resource
    private TCloudAgreementMapper cloudAgreementMapper;

    public CloudAgreementRepository() {
        init("cloudAgreement::");
    }

    public TCloudAgreement find(int cid) {
        TCloudAgreement cloudAgreement = getCache(cid, TCloudAgreement.class);
        if (null != cloudAgreement) {
            return cloudAgreement;
        }

        // 缓存没有就查询数据库
        TCloudAgreementExample example = new TCloudAgreementExample();
        example.or().andCidEqualTo(cid);
        cloudAgreement = cloudAgreementMapper.selectOneByExample(example);
        if (null != cloudAgreement) {
            setCache(cid, cloudAgreement);
        }
        return cloudAgreement;
    }

    public List<TCloudAgreement> findByPid(int pid) {
        TCloudAgreementExample example = new TCloudAgreementExample();
        example.or().andPidEqualTo(pid);
        return cloudAgreementMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TCloudAgreementExample example = new TCloudAgreementExample();
        example.or().andPidEqualTo(pid);
        return null != cloudAgreementMapper.selectOneByExample(example);
    }

    public boolean insert(int cid, int pid) {
        TCloudAgreement row = new TCloudAgreement();
        row.setCid(cid);
        row.setPid(pid);
        if (cloudAgreementMapper.insert(row) > 0) {
            setCache(cid, row);
            return true;
        }
        return false;
    }

    public boolean update(TCloudAgreement row) {
        if (cloudAgreementMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid, int pid) {
        delCache(cid);
        TCloudAgreementExample example = new TCloudAgreementExample();
        example.or().andCidEqualTo(cid).andPidEqualTo(pid);
        return cloudAgreementMapper.deleteByExample(example) > 0;
    }
}
