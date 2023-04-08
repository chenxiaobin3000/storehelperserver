package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSaleReturnMapper;
import com.cxb.storehelperserver.model.TSaleReturn;
import com.cxb.storehelperserver.model.TSaleReturnExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 线上售后关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class SaleReturnRepository extends BaseRepository<TSaleReturn> {
    @Resource
    private TSaleReturnMapper saleReturnMapper;

    public SaleReturnRepository() {
        init("saleReturn::");
    }

    public TSaleReturn find(int oid) {
        TSaleReturn saleReturn = getCache(oid, TSaleReturn.class);
        if (null != saleReturn) {
            return saleReturn;
        }

        // 缓存没有就查询数据库
        TSaleReturnExample example = new TSaleReturnExample();
        example.or().andOidEqualTo(oid);
        saleReturn = saleReturnMapper.selectOneByExample(example);
        if (null != saleReturn) {
            setCache(oid, saleReturn);
        }
        return saleReturn;
    }

    public List<TSaleReturn> findByAid(int aid) {
        TSaleReturnExample example = new TSaleReturnExample();
        example.or().andAidEqualTo(aid);
        return saleReturnMapper.selectByExample(example);
    }

    public boolean checkByAid(int aid) {
        TSaleReturnExample example = new TSaleReturnExample();
        example.or().andAidEqualTo(aid);
        return null != saleReturnMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int aid) {
        TSaleReturn row = new TSaleReturn();
        row.setOid(oid);
        row.setAid(aid);
        if (saleReturnMapper.insert(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSaleReturn row) {
        if (saleReturnMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TSaleReturn saleReturn = saleReturnMapper.selectByPrimaryKey(id);
        if (null == saleReturn) {
            return false;
        }
        delCache(saleReturn.getOid());
        return saleReturnMapper.deleteByPrimaryKey(id) > 0;
    }
}
