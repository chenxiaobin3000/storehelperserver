package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TLossCommodityMapper;
import com.cxb.storehelperserver.model.TLossCommodity;
import com.cxb.storehelperserver.model.TLossCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyLossCommodityMapper;
import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 损耗出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class LossCommodityRepository extends BaseRepository<List> {
    @Resource
    private TLossCommodityMapper lossCommodityMapper;

    @Resource
    private MyLossCommodityMapper myLossCommodityMapper;

    public LossCommodityRepository() {
        init("lossComm::");
    }

    public List<TLossCommodity> find(int oid) {
        List<TLossCommodity> lossCommoditys = getCache(oid, List.class);
        if (null != lossCommoditys) {
            return lossCommoditys;
        }

        // 缓存没有就查询数据库
        TLossCommodityExample example = new TLossCommodityExample();
        example.or().andOidEqualTo(oid);
        lossCommoditys = lossCommodityMapper.selectByExample(example);
        if (null != lossCommoditys) {
            setCache(oid, lossCommoditys);
        }
        return lossCommoditys;
    }

    public List<MyOrderCommodity> findByGid(int gid, Date start, Date end) {
        return myLossCommodityMapper.selectByGid(gid, start, end);
    }

    public List<MyOrderCommodity> findBySid(int sid, Date start, Date end) {
        return myLossCommodityMapper.selectBySid(sid, start, end);
    }

    // 注意：数据被缓存在LossCommodityService，所以不能直接调用该函数
    public boolean update(List<TLossCommodity> rows, int oid) {
        delete(oid);
        for (TLossCommodity productCommodity : rows) {
            if (lossCommodityMapper.insert(productCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TLossCommodityExample example = new TLossCommodityExample();
        example.or().andOidEqualTo(oid);
        return lossCommodityMapper.deleteByExample(example) > 0;
    }
}
