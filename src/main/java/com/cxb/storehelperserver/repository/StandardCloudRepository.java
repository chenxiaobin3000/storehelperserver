package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStandardCloudMapper;
import com.cxb.storehelperserver.model.TStandardCloud;
import com.cxb.storehelperserver.model.TStandardCloudExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 标品云仓关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StandardCloudRepository extends BaseRepository<TStandardCloud> {
    @Resource
    private TStandardCloudMapper commodityCloudMapper;

    public StandardCloudRepository() {
        init("stanCloud::");
    }

    public TStandardCloud find(int cid) {
        TStandardCloud commodityCloud = getCache(cid, TStandardCloud.class);
        if (null != commodityCloud) {
            return commodityCloud;
        }

        // 缓存没有就查询数据库
        TStandardCloudExample example = new TStandardCloudExample();
        example.or().andCidEqualTo(cid);
        commodityCloud = commodityCloudMapper.selectOneByExample(example);
        if (null != commodityCloud) {
            setCache(cid, commodityCloud);
        }
        return commodityCloud;
    }

    public boolean insert(TStandardCloud row) {
        if (commodityCloudMapper.insert(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStandardCloud row) {
        if (commodityCloudMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TStandardCloudExample example = new TStandardCloudExample();
        example.or().andCidEqualTo(cid);
        return commodityCloudMapper.deleteByExample(example) > 0;
    }
}
