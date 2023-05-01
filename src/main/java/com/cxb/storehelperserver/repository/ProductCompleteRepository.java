package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductCompleteMapper;
import com.cxb.storehelperserver.model.TProductComplete;
import com.cxb.storehelperserver.model.TProductCompleteExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 生产结算关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class ProductCompleteRepository extends BaseRepository<TProductComplete> {
    @Resource
    private TProductCompleteMapper productCompleteMapper;

    public ProductCompleteRepository() {
        init("productComplete::");
    }

    public TProductComplete find(int oid) {
        TProductComplete productComplete = getCache(oid, TProductComplete.class);
        if (null != productComplete) {
            return productComplete;
        }

        // 缓存没有就查询数据库
        TProductCompleteExample example = new TProductCompleteExample();
        example.or().andOidEqualTo(oid);
        productComplete = productCompleteMapper.selectOneByExample(example);
        if (null != productComplete) {
            setCache(oid, productComplete);
        }
        return productComplete;
    }

    public boolean insert(int oid, int pid) {
        TProductComplete row = new TProductComplete();
        row.setOid(oid);
        row.setPid(pid);
        if (productCompleteMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TProductComplete row) {
        if (productCompleteMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int pid) {
        delCache(oid);
        TProductCompleteExample example = new TProductCompleteExample();
        example.or().andOidEqualTo(oid).andPidEqualTo(pid);
        return productCompleteMapper.deleteByExample(example) > 0;
    }
}
