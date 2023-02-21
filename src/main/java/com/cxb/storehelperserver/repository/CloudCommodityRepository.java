package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudCommodityMapper;
import com.cxb.storehelperserver.model.TCloudCommodity;
import com.cxb.storehelperserver.model.TCloudCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyCloudCommodityMapper;
import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 云仓出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class CloudCommodityRepository extends BaseRepository<List> {
    @Resource
    private TCloudCommodityMapper cloudCommodityMapper;

    @Resource
    private MyCloudCommodityMapper myCloudCommodityMapper;

    public CloudCommodityRepository() {
        init("cloudComm::");
    }

    public List<TCloudCommodity> find(int oid) {
        List<TCloudCommodity> cloudCommoditys = getCache(oid, List.class);
        if (null != cloudCommoditys) {
            return cloudCommoditys;
        }

        // 缓存没有就查询数据库
        TCloudCommodityExample example = new TCloudCommodityExample();
        example.or().andOidEqualTo(oid);
        cloudCommoditys = cloudCommodityMapper.selectByExample(example);
        if (null != cloudCommoditys) {
            setCache(oid, cloudCommoditys);
        }
        return cloudCommoditys;
    }

    public List<MyOrderCommodity> findByGid(int gid, Date start, Date end) {
        return myCloudCommodityMapper.selectByGid(gid, start, end);
    }

    public List<MyOrderCommodity> findBySid(int sid, Date start, Date end) {
        return myCloudCommodityMapper.selectBySid(sid, start, end);
    }

    // 注意：数据被缓存在CloudCommodityService，所以不能直接调用该函数
    public boolean update(List<TCloudCommodity> rows, int oid) {
        delete(oid);
        for (TCloudCommodity productCommodity : rows) {
            if (cloudCommodityMapper.insert(productCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TCloudCommodityExample example = new TCloudCommodityExample();
        example.or().andOidEqualTo(oid);
        return cloudCommodityMapper.deleteByExample(example) > 0;
    }
}
