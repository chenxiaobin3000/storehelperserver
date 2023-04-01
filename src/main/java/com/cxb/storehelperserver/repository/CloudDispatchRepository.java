package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudDispatchMapper;
import com.cxb.storehelperserver.model.TCloudDispatch;
import com.cxb.storehelperserver.model.TCloudDispatchExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 云仓与调度关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CloudDispatchRepository extends BaseRepository<TCloudDispatch> {
    @Resource
    private TCloudDispatchMapper cloudDispatchMapper;

    public CloudDispatchRepository() {
        init("cloudDispatch::");
    }

    public TCloudDispatch find(int oid) {
        TCloudDispatch cloudDispatch = getCache(oid, TCloudDispatch.class);
        if (null != cloudDispatch) {
            return cloudDispatch;
        }

        // 缓存没有就查询数据库
        TCloudDispatchExample example = new TCloudDispatchExample();
        example.or().andOidEqualTo(oid);
        cloudDispatch = cloudDispatchMapper.selectOneByExample(example);
        if (null != cloudDispatch) {
            setCache(oid, cloudDispatch);
        }
        return cloudDispatch;
    }

    public List<TCloudDispatch> findByDid(int did) {
        TCloudDispatchExample example = new TCloudDispatchExample();
        example.or().andDidEqualTo(did);
        return cloudDispatchMapper.selectByExample(example);
    }

    public boolean checkByDid(int did) {
        TCloudDispatchExample example = new TCloudDispatchExample();
        example.or().andDidEqualTo(did);
        return null != cloudDispatchMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int did) {
        TCloudDispatch row = new TCloudDispatch();
        row.setOid(oid);
        row.setDid(did);
        if (cloudDispatchMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TCloudDispatch row) {
        if (cloudDispatchMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int did) {
        delCache(oid);
        TCloudDispatchExample example = new TCloudDispatchExample();
        example.or().andOidEqualTo(oid).andDidEqualTo(did);
        return cloudDispatchMapper.deleteByExample(example) > 0;
    }
}
