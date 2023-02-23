package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudFareMapper;
import com.cxb.storehelperserver.model.TCloudFare;
import com.cxb.storehelperserver.model.TCloudFareExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
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

    public CloudFareRepository() {
        init("cloudFare::");
    }

    public List<TCloudFare> find(int oid) {
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

    public boolean insert(int oid, BigDecimal fare) {
        TCloudFare row = new TCloudFare();
        row.setOid(oid);
        row.setFare(fare);
        if (cloudFareMapper.insert(row) > 0) {
            delCache(row.getOid());
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
}
