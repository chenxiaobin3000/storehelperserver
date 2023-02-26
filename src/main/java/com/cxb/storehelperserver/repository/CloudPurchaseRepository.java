package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudPurchaseMapper;
import com.cxb.storehelperserver.model.TCloudPurchase;
import com.cxb.storehelperserver.model.TCloudPurchaseExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 入库与采购关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CloudPurchaseRepository extends BaseRepository<TCloudPurchase> {
    @Resource
    private TCloudPurchaseMapper cloudPurchaseMapper;

    public CloudPurchaseRepository() {
        init("cloudPurchase::");
    }

    public TCloudPurchase find(int cid) {
        TCloudPurchase cloudPurchase = getCache(cid, TCloudPurchase.class);
        if (null != cloudPurchase) {
            return cloudPurchase;
        }

        // 缓存没有就查询数据库
        TCloudPurchaseExample example = new TCloudPurchaseExample();
        example.or().andCidEqualTo(cid);
        cloudPurchase = cloudPurchaseMapper.selectOneByExample(example);
        if (null != cloudPurchase) {
            setCache(cid, cloudPurchase);
        }
        return cloudPurchase;
    }

    public List<TCloudPurchase> findByPid(int pid) {
        TCloudPurchaseExample example = new TCloudPurchaseExample();
        example.or().andPidEqualTo(pid);
        return cloudPurchaseMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TCloudPurchaseExample example = new TCloudPurchaseExample();
        example.or().andPidEqualTo(pid);
        return null != cloudPurchaseMapper.selectOneByExample(example);
    }

    public boolean insert(int cid, int pid) {
        TCloudPurchase row = new TCloudPurchase();
        row.setCid(cid);
        row.setPid(pid);
        if (cloudPurchaseMapper.insert(row) > 0) {
            setCache(cid, row);
            return true;
        }
        return false;
    }

    public boolean update(TCloudPurchase row) {
        if (cloudPurchaseMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid, int pid) {
        delCache(cid);
        TCloudPurchaseExample example = new TCloudPurchaseExample();
        example.or().andCidEqualTo(cid).andPidEqualTo(pid);
        return cloudPurchaseMapper.deleteByExample(example) > 0;
    }
}
