package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.THalfgoodCloudMapper;
import com.cxb.storehelperserver.model.THalfgoodCloud;
import com.cxb.storehelperserver.model.THalfgoodCloudExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 半成品云仓关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class HalfgoodCloudRepository extends BaseRepository<THalfgoodCloud> {
    @Resource
    private THalfgoodCloudMapper commodityCloudMapper;

    public HalfgoodCloudRepository() {
        init("halfCloud::");
    }

    public THalfgoodCloud find(int cid) {
        THalfgoodCloud commodityCloud = getCache(cid, THalfgoodCloud.class);
        if (null != commodityCloud) {
            return commodityCloud;
        }

        // 缓存没有就查询数据库
        THalfgoodCloudExample example = new THalfgoodCloudExample();
        example.or().andCidEqualTo(cid);
        commodityCloud = commodityCloudMapper.selectOneByExample(example);
        if (null != commodityCloud) {
            setCache(cid, commodityCloud);
        }
        return commodityCloud;
    }

    public boolean insert(THalfgoodCloud row) {
        if (commodityCloudMapper.insert(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean update(THalfgoodCloud row) {
        if (commodityCloudMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        THalfgoodCloudExample example = new THalfgoodCloudExample();
        example.or().andCidEqualTo(cid);
        return commodityCloudMapper.deleteByExample(example) > 0;
    }
}
