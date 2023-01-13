package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStandardAttrMapper;
import com.cxb.storehelperserver.model.TStandardAttr;
import com.cxb.storehelperserver.model.TStandardAttrExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 标品属性仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class StandardAttrRepository extends BaseRepository<List> {
    @Resource
    private TStandardAttrMapper standardAttrMapper;

    public StandardAttrRepository() {
        init("stanAttr::");
    }

    public List<TStandardAttr> find(int sid) {
        List<TStandardAttr> commoditieAtrrs = getCache(sid, List.class);
        if (null != commoditieAtrrs) {
            return commoditieAtrrs;
        }
        TStandardAttrExample example = new TStandardAttrExample();
        example.or().andSidEqualTo(sid);
        example.setOrderByClause("idx asc");
        commoditieAtrrs = standardAttrMapper.selectByExample(example);
        if (null != commoditieAtrrs) {
            setCache(sid, commoditieAtrrs);
        }
        return commoditieAtrrs;
    }

    public boolean update(int sid, List<String> standardAtrrs) {
        TStandardAttrExample example = new TStandardAttrExample();
        example.or().andSidEqualTo(sid);
        standardAttrMapper.deleteByExample(example);

        int index = 0;
        val list = new ArrayList<TStandardAttr>();
        for (String attr : standardAtrrs) {
            TStandardAttr standardAttr = new TStandardAttr();
            standardAttr.setSid(sid);
            standardAttr.setIdx(++index);
            standardAttr.setValue(attr);
            if (standardAttrMapper.insert(standardAttr) < 0) {
                return false;
            }
            list.add(standardAttr);
        }
        setCache(sid, list);
        return true;
    }

    public boolean delete(int sid) {
        delCache(sid);
        TStandardAttrExample example = new TStandardAttrExample();
        example.or().andSidEqualTo(sid);
        return standardAttrMapper.deleteByExample(example) > 0;
    }
}
