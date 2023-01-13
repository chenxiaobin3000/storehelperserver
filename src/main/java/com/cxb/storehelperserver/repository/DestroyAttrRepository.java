package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TDestroyAttrMapper;
import com.cxb.storehelperserver.model.TDestroyAttr;
import com.cxb.storehelperserver.model.TDestroyAttrExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 废料属性仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class DestroyAttrRepository extends BaseRepository<List> {
    @Resource
    private TDestroyAttrMapper destroyAttrMapper;

    public DestroyAttrRepository() {
        init("destAttr::");
    }

    public List<TDestroyAttr> find(int did) {
        List<TDestroyAttr> commoditieAtrrs = getCache(did, List.class);
        if (null != commoditieAtrrs) {
            return commoditieAtrrs;
        }
        TDestroyAttrExample example = new TDestroyAttrExample();
        example.or().andDidEqualTo(did);
        example.setOrderByClause("idx asc");
        commoditieAtrrs = destroyAttrMapper.selectByExample(example);
        if (null != commoditieAtrrs) {
            setCache(did, commoditieAtrrs);
        }
        return commoditieAtrrs;
    }

    public boolean update(int did, List<String> destroyAtrrs) {
        TDestroyAttrExample example = new TDestroyAttrExample();
        example.or().andDidEqualTo(did);
        destroyAttrMapper.deleteByExample(example);

        int index = 0;
        val list = new ArrayList<TDestroyAttr>();
        for (String attr : destroyAtrrs) {
            TDestroyAttr destroyAttr = new TDestroyAttr();
            destroyAttr.setDid(did);
            destroyAttr.setIdx(++index);
            destroyAttr.setValue(attr);
            if (destroyAttrMapper.insert(destroyAttr) < 0) {
                return false;
            }
            list.add(destroyAttr);
        }
        setCache(did, list);
        return true;
    }

    public boolean delete(int did) {
        delCache(did);
        TDestroyAttrExample example = new TDestroyAttrExample();
        example.or().andDidEqualTo(did);
        return destroyAttrMapper.deleteByExample(example) > 0;
    }
}
