package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSaleCommodityMapper;
import com.cxb.storehelperserver.model.TSaleCommodity;
import com.cxb.storehelperserver.model.TSaleCommodityExample;
import com.cxb.storehelperserver.model.TSaleOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

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

    public int total(int gid, int type, ReviewType review, String search, int cid) {
        return 0;
    }

    public List<TSaleOrder> pagination(int gid, int type, int page, int limit, ReviewType review, String date, int cid) {
        return null;
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
