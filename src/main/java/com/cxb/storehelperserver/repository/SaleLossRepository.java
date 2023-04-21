package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSaleLossMapper;
import com.cxb.storehelperserver.model.TSaleLoss;
import com.cxb.storehelperserver.model.TSaleLossExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 销售损耗关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class SaleLossRepository extends BaseRepository<TSaleLoss> {
    @Resource
    private TSaleLossMapper saleLossMapper;

    public SaleLossRepository() {
        init("saleLoss::");
    }

    public TSaleLoss find(int oid) {
        TSaleLoss saleLoss = getCache(oid, TSaleLoss.class);
        if (null != saleLoss) {
            return saleLoss;
        }

        // 缓存没有就查询数据库
        TSaleLossExample example = new TSaleLossExample();
        example.or().andOidEqualTo(oid);
        saleLoss = saleLossMapper.selectOneByExample(example);
        if (null != saleLoss) {
            setCache(oid, saleLoss);
        }
        return saleLoss;
    }

    public List<TSaleLoss> findByAid(int aid) {
        TSaleLossExample example = new TSaleLossExample();
        example.or().andAidEqualTo(aid);
        return saleLossMapper.selectByExample(example);
    }

    public boolean checkByAid(int aid) {
        TSaleLossExample example = new TSaleLossExample();
        example.or().andAidEqualTo(aid);
        return null != saleLossMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int aid) {
        TSaleLoss row = new TSaleLoss();
        row.setOid(oid);
        row.setAid(aid);
        if (saleLossMapper.insert(row) > 0) {
            setCache(oid, row);
            return true;
        }
        return false;
    }

    public boolean update(TSaleLoss row) {
        if (saleLossMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TSaleLoss saleLoss = saleLossMapper.selectByPrimaryKey(id);
        if (null == saleLoss) {
            return false;
        }
        delCache(saleLoss.getOid());
        return saleLossMapper.deleteByPrimaryKey(id) > 0;
    }
}
