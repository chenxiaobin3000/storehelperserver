package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalAttrMapper;
import com.cxb.storehelperserver.model.TOriginalAttr;
import com.cxb.storehelperserver.model.TOriginalAttrExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 原料属性仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class OriginalAttrRepository extends BaseRepository<List> {
    @Resource
    private TOriginalAttrMapper originalAttrMapper;

    public OriginalAttrRepository() {
        init("oriAttr::");
    }

    public List<TOriginalAttr> find(int oid) {
        List<TOriginalAttr> originalAttrs = getCache(oid, List.class);
        if (null != originalAttrs) {
            return originalAttrs;
        }
        TOriginalAttrExample example = new TOriginalAttrExample();
        example.or().andOidEqualTo(oid);
        example.setOrderByClause("idx asc");
        originalAttrs = originalAttrMapper.selectByExample(example);
        if (null != originalAttrs) {
            setCache(oid, originalAttrs);
        }
        return originalAttrs;
    }

    public boolean update(int oid, List<String> originalAtrrs) {
        TOriginalAttrExample example = new TOriginalAttrExample();
        example.or().andOidEqualTo(oid);
        originalAttrMapper.deleteByExample(example);

        int index = 0;
        val list = new ArrayList<TOriginalAttr>();
        for (String attr : originalAtrrs) {
            TOriginalAttr originalAttr = new TOriginalAttr();
            originalAttr.setOid(oid);
            originalAttr.setIdx(++index);
            originalAttr.setValue(attr);
            if (originalAttrMapper.insert(originalAttr) < 0) {
                return false;
            }
            list.add(originalAttr);
        }
        setCache(oid, list);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TOriginalAttrExample example = new TOriginalAttrExample();
        example.or().andOidEqualTo(oid);
        return originalAttrMapper.deleteByExample(example) > 0;
    }
}
