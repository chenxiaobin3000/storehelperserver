package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudFareMapper;
import com.cxb.storehelperserver.model.TCloudFare;
import com.cxb.storehelperserver.model.TCloudFareExample;
import com.cxb.storehelperserver.repository.mapper.MyFareMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * desc: 云仓物流费用仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CloudFareRepository extends BaseRepository<List> {
    @Resource
    private TCloudFareMapper cloudFareMapper;

    @Resource
    private MyFareMapper myFareMapper;

    public CloudFareRepository() {
        init("cloudFare::");
    }

    public TCloudFare find(int id) {
        return cloudFareMapper.selectByPrimaryKey(id);
    }

    public List<TCloudFare> findByOid(int oid) {
        List<TCloudFare> cloudFares = getCache(oid, List.class);
        if (null != cloudFares) {
            return cloudFares;
        }

        // 缓存没有就查询数据库
        TCloudFareExample example = new TCloudFareExample();
        example.or().andOidEqualTo(oid);
        cloudFares = cloudFareMapper.selectByExample(example);
        if (null != cloudFares) {
            setCache(oid, cloudFares);
        }
        return cloudFares;
    }

    public boolean insert(int oid, BigDecimal fare, Date cdate) {
        TCloudFare row = new TCloudFare();
        row.setOid(oid);
        row.setFare(fare);
        row.setCdate(cdate);
        if (cloudFareMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TCloudFare row) {
        if (cloudFareMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TCloudFare cloudFare = cloudFareMapper.selectByPrimaryKey(id);
        if (null == cloudFare) {
            return false;
        }
        delCache(cloudFare.getOid());
        return cloudFareMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean setReviewNull(int oid) {
        delCache(oid);
        return myFareMapper.setCloudFareReviewNull(oid) > 0;
    }
}
