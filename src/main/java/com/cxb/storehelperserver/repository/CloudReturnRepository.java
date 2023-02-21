package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudReturnMapper;
import com.cxb.storehelperserver.model.TCloudReturn;
import com.cxb.storehelperserver.model.TCloudReturnExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 云仓退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CloudReturnRepository extends BaseRepository<TCloudReturn> {
    @Resource
    private TCloudReturnMapper cloudReturnMapper;

    public CloudReturnRepository() {
        init("cloudRet::");
    }

    public TCloudReturn find(int oid) {
        TCloudReturn cloudReturn = getCache(oid, TCloudReturn.class);
        if (null != cloudReturn) {
            return cloudReturn;
        }

        // 缓存没有就查询数据库
        TCloudReturnExample example = new TCloudReturnExample();
        example.or().andOidEqualTo(oid);
        cloudReturn = cloudReturnMapper.selectOneByExample(example);
        if (null != cloudReturn) {
            setCache(oid, cloudReturn);
        }
        return cloudReturn;
    }

    public List<TCloudReturn> findByPid(int pid) {
        TCloudReturnExample example = new TCloudReturnExample();
        example.or().andPidEqualTo(pid);
        return cloudReturnMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TCloudReturnExample example = new TCloudReturnExample();
        example.or().andPidEqualTo(pid);
        return null != cloudReturnMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int pid) {
        TCloudReturn row = new TCloudReturn();
        row.setOid(oid);
        row.setPid(pid);
        if (cloudReturnMapper.insert(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TCloudReturn row) {
        if (cloudReturnMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TCloudReturn cloudReturn = cloudReturnMapper.selectByPrimaryKey(id);
        if (null == cloudReturn) {
            return false;
        }
        delCache(cloudReturn.getOid());
        return cloudReturnMapper.deleteByPrimaryKey(id) > 0;
    }
}
