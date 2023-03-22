package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSaleCommodityMapper;
import com.cxb.storehelperserver.model.TSaleCommodity;
import com.cxb.storehelperserver.model.TSaleCommodityExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 销售出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SaleCommodityRepository extends BaseRepository<List> {
    @Resource
    private TSaleCommodityMapper saleCommodityMapper;

    public SaleCommodityRepository() {
        init("saleComm::");
    }

    public TSaleCommodity findOne(int oid, int ctype, int cid) {
        List<TSaleCommodity> saleCommoditys = getCache(oid, List.class);
        if (null != saleCommoditys) {
            for (TSaleCommodity c : saleCommoditys) {
                if (c.getCtype() == ctype && c.getCid() == cid) {
                    return c;
                }
            }
        }

        // 缓存没有就查询数据库
        TSaleCommodityExample example = new TSaleCommodityExample();
        example.or().andOidEqualTo(oid).andCtypeEqualTo(ctype).andCidEqualTo(cid);
        return saleCommodityMapper.selectOneByExample(example);
    }

    public List<TSaleCommodity> find(int oid) {
        List<TSaleCommodity> saleCommoditys = getCache(oid, List.class);
        if (null != saleCommoditys) {
            return saleCommoditys;
        }

        // 缓存没有就查询数据库
        TSaleCommodityExample example = new TSaleCommodityExample();
        example.or().andOidEqualTo(oid);
        saleCommoditys = saleCommodityMapper.selectByExample(example);
        if (null != saleCommoditys) {
            setCache(oid, saleCommoditys);
        }
        return saleCommoditys;
    }

    // 注意：数据被缓存在SaleCommodityService，所以不能直接调用该函数
    public boolean update(List<TSaleCommodity> rows, int oid) {
        delete(oid);
        for (TSaleCommodity saleCommodity : rows) {
            if (saleCommodityMapper.insert(saleCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TSaleCommodityExample example = new TSaleCommodityExample();
        example.or().andOidEqualTo(oid);
        return saleCommodityMapper.deleteByExample(example) > 0;
    }
}
