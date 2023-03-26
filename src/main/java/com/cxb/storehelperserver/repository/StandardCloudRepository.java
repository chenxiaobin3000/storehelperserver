package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStandardCloudMapper;
import com.cxb.storehelperserver.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 标品云仓关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StandardCloudRepository extends BaseRepository<List> {
    @Resource
    private TStandardCloudMapper standardCloudMapper;

    public StandardCloudRepository() {
        init("stanCloud::");
    }

    public List<TStandardCloud> find(int cid) {
        List<TStandardCloud> standardClouds = getCache(cid, List.class);
        if (null != standardClouds) {
            return standardClouds;
        }

        // 缓存没有就查询数据库
        TStandardCloudExample example = new TStandardCloudExample();
        example.or().andCidEqualTo(cid);
        standardClouds = standardCloudMapper.selectByExample(example);
        if (null != standardClouds) {
            setCache(cid, standardClouds);
        }
        return standardClouds;
    }

    public boolean update(int cid, List<Integer> sids) {
        delete(cid);
        TStandardCloud row = new TStandardCloud();
        row.setCid(cid);
        for (Integer sid : sids) {
            row.setId(0);
            row.setSid(sid);
            if (standardCloudMapper.insert(row) <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TStandardCloudExample example = new TStandardCloudExample();
        example.or().andCidEqualTo(cid);
        return standardCloudMapper.deleteByExample(example) > 0;
    }
}
