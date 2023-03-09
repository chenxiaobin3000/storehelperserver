package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TProductCompleteMapper;
import com.cxb.storehelperserver.model.TProductComplete;
import com.cxb.storehelperserver.model.TProductCompleteExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 生产完成关联仓库
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

    public TProductComplete find(int cid) {
        TProductComplete productComplete = getCache(cid, TProductComplete.class);
        if (null != productComplete) {
            return productComplete;
        }

        // 缓存没有就查询数据库
        TProductCompleteExample example = new TProductCompleteExample();
        example.or().andCidEqualTo(cid);
        productComplete = productCompleteMapper.selectOneByExample(example);
        if (null != productComplete) {
            setCache(cid, productComplete);
        }
        return productComplete;
    }

    public List<TProductComplete> findByPid(int pid) {
        TProductCompleteExample example = new TProductCompleteExample();
        example.or().andPidEqualTo(pid);
        return productCompleteMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TProductCompleteExample example = new TProductCompleteExample();
        example.or().andPidEqualTo(pid);
        return null != productCompleteMapper.selectOneByExample(example);
    }

    public boolean insert(int cid, int pid) {
        TProductComplete row = new TProductComplete();
        row.setCid(cid);
        row.setPid(pid);
        if (productCompleteMapper.insert(row) > 0) {
            setCache(cid, row);
            return true;
        }
        return false;
    }

    public boolean update(TProductComplete row) {
        if (productCompleteMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getCid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int cid, int pid) {
        delCache(cid);
        TProductCompleteExample example = new TProductCompleteExample();
        example.or().andCidEqualTo(cid).andPidEqualTo(pid);
        return productCompleteMapper.deleteByExample(example) > 0;
    }
}
