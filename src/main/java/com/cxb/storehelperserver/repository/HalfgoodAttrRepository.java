package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.THalfgoodAttrMapper;
import com.cxb.storehelperserver.model.THalfgoodAttr;
import com.cxb.storehelperserver.model.THalfgoodAttrExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 半成品属性仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class HalfgoodAttrRepository extends BaseRepository<List> {
    @Resource
    private THalfgoodAttrMapper halfgoodAttrMapper;

    public HalfgoodAttrRepository() {
        init("halfAttr::");
    }

    public List<THalfgoodAttr> find(int hid) {
        List<THalfgoodAttr> commoditieAtrrs = getCache(hid, List.class);
        if (null != commoditieAtrrs) {
            return commoditieAtrrs;
        }
        THalfgoodAttrExample example = new THalfgoodAttrExample();
        example.or().andHidEqualTo(hid);
        example.setOrderByClause("idx asc");
        commoditieAtrrs = halfgoodAttrMapper.selectByExample(example);
        if (null != commoditieAtrrs) {
            setCache(hid, commoditieAtrrs);
        }
        return commoditieAtrrs;
    }

    public boolean update(int hid, List<String> halfgoodAtrrs) {
        THalfgoodAttrExample example = new THalfgoodAttrExample();
        example.or().andHidEqualTo(hid);
        halfgoodAttrMapper.deleteByExample(example);

        int index = 0;
        val list = new ArrayList<THalfgoodAttr>();
        for (String attr : halfgoodAtrrs) {
            THalfgoodAttr halfgoodAttr = new THalfgoodAttr();
            halfgoodAttr.setHid(hid);
            halfgoodAttr.setIdx(++index);
            halfgoodAttr.setValue(attr);
            if (halfgoodAttrMapper.insert(halfgoodAttr) < 0) {
                return false;
            }
            list.add(halfgoodAttr);
        }
        setCache(hid, list);
        return true;
    }

    public boolean delete(int hid) {
        delCache(hid);
        THalfgoodAttrExample example = new THalfgoodAttrExample();
        example.or().andHidEqualTo(hid);
        return halfgoodAttrMapper.deleteByExample(example) > 0;
    }
}
