package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityAttrMapper;
import com.cxb.storehelperserver.model.TCommodityAttr;
import com.cxb.storehelperserver.model.TCommodityAttrExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 商品属性仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class CommodityAttrRepository extends BaseRepository<List> {
    @Resource
    private TCommodityAttrMapper commodityAttrMapper;

    public CommodityAttrRepository() {
        init("commAttr::");
    }

    public List<TCommodityAttr> find(int cid) {
        List<TCommodityAttr> commoditieAtrrs = getCache(cid, List.class);
        if (null != commoditieAtrrs) {
            return commoditieAtrrs;
        }
        TCommodityAttrExample example = new TCommodityAttrExample();
        example.or().andCidEqualTo(cid);
        example.setOrderByClause("idx asc");
        commoditieAtrrs = commodityAttrMapper.selectByExample(example);
        if (null != commoditieAtrrs) {
            setCache(cid, commoditieAtrrs);
        }
        return commoditieAtrrs;
    }

    public boolean update(int cid, List<String> commodityAtrrs) {
        TCommodityAttrExample example = new TCommodityAttrExample();
        example.or().andCidEqualTo(cid);
        commodityAttrMapper.deleteByExample(example);

        int index = 0;
        val list = new ArrayList<TCommodityAttr>();
        for (String attr : commodityAtrrs) {
            TCommodityAttr commodityAttr = new TCommodityAttr();
            commodityAttr.setCid(cid);
            commodityAttr.setIdx(++index);
            commodityAttr.setValue(attr);
            if (commodityAttrMapper.insert(commodityAttr) < 0) {
                return false;
            }
            list.add(commodityAttr);
        }
        setCache(cid, list);
        return true;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TCommodityAttrExample example = new TCommodityAttrExample();
        example.or().andCidEqualTo(cid);
        return commodityAttrMapper.deleteByExample(example) > 0;
    }
}
