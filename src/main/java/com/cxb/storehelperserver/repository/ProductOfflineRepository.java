package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductOfflineMapper;
import com.cxb.storehelperserver.model.TProductOffline;
import com.cxb.storehelperserver.model.TProductOfflineExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 生产与线下销售关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class ProductOfflineRepository extends BaseRepository<TProductOffline> {
    @Resource
    private TProductOfflineMapper productOfflineMapper;

    public ProductOfflineRepository() {
        init("productOffline::");
    }

    public TProductOffline find(int oid) {
        TProductOffline productOffline = getCache(oid, TProductOffline.class);
        if (null != productOffline) {
            return productOffline;
        }

        // 缓存没有就查询数据库
        TProductOfflineExample example = new TProductOfflineExample();
        example.or().andOidEqualTo(oid);
        productOffline = productOfflineMapper.selectOneByExample(example);
        if (null != productOffline) {
            setCache(oid, productOffline);
        }
        return productOffline;
    }

    public boolean insert(int oid, int sid) {
        TProductOffline row = new TProductOffline();
        row.setOid(oid);
        row.setSid(sid);
        if (productOfflineMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TProductOffline row) {
        if (productOfflineMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int sid) {
        delCache(oid);
        TProductOfflineExample example = new TProductOfflineExample();
        example.or().andOidEqualTo(oid).andSidEqualTo(sid);
        return productOfflineMapper.deleteByExample(example) > 0;
    }
}
