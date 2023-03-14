package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalCloudMapper;
import com.cxb.storehelperserver.model.TOriginalCloud;
import com.cxb.storehelperserver.model.TOriginalCloudExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 废料云仓关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OriginalCloudRepository extends BaseRepository<TOriginalCloud> {
    @Resource
    private TOriginalCloudMapper commodityCloudMapper;

    public OriginalCloudRepository() {
        init("oriCloud::");
    }

    public TOriginalCloud find(int cid) {
        TOriginalCloud commodityCloud = getCache(cid, TOriginalCloud.class);
        if (null != commodityCloud) {
            return commodityCloud;
        }

        // 缓存没有就查询数据库
        TOriginalCloudExample example = new TOriginalCloudExample();
        example.or().andCidEqualTo(cid);
        commodityCloud = commodityCloudMapper.selectOneByExample(example);
        if (null != commodityCloud) {
            setCache(cid, commodityCloud);
        }
        return commodityCloud;
    }

    public boolean insert(TOriginalCloud row) {
        if (commodityCloudMapper.insert(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TOriginalCloud row) {
        if (commodityCloudMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TOriginalCloudExample example = new TOriginalCloudExample();
        example.or().andCidEqualTo(cid);
        return commodityCloudMapper.deleteByExample(example) > 0;
    }
}
