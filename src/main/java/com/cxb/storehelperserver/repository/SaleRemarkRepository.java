package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSaleRemarkMapper;
import com.cxb.storehelperserver.model.TSaleRemark;
import com.cxb.storehelperserver.model.TSaleRemarkExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 销售备注关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class SaleRemarkRepository extends BaseRepository<List> {
    @Resource
    private TSaleRemarkMapper saleRemarkMapper;

    public SaleRemarkRepository() {
        init("saleRemark::");
    }

    public TSaleRemark find(int id) {
        return saleRemarkMapper.selectByPrimaryKey(id);
    }

    public List<TSaleRemark> findByOid(int oid) {
        List<TSaleRemark> saleRemarks = getCache(oid, List.class);
        if (null != saleRemarks) {
            return saleRemarks;
        }

        // 缓存没有就查询数据库
        TSaleRemarkExample example = new TSaleRemarkExample();
        example.or().andOidEqualTo(oid);
        saleRemarks = saleRemarkMapper.selectByExample(example);
        if (null != saleRemarks) {
            setCache(oid, saleRemarks);
        }
        return saleRemarks;
    }

    public boolean insert(int oid, String remark, Date cdate) {
        TSaleRemark row = new TSaleRemark();
        row.setOid(oid);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (saleRemarkMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TSaleRemark row) {
        if (saleRemarkMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TSaleRemark saleRemark = saleRemarkMapper.selectByPrimaryKey(id);
        if (null == saleRemark) {
            return false;
        }
        delCache(saleRemark.getOid());
        return saleRemarkMapper.deleteByPrimaryKey(id) > 0;
    }
}
