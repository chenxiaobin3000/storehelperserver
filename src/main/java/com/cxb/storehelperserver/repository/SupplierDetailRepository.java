package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSupplierDetailMapper;
import com.cxb.storehelperserver.model.TSupplierDetail;
import com.cxb.storehelperserver.model.TSupplierDetailExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 供应商货款明细仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class SupplierDetailRepository extends BaseRepository<TSupplierDetail> {
    @Resource
    private TSupplierDetailMapper supplierDetailMapper;

    public SupplierDetailRepository() {
        init("supplierDetail::");
    }

    public TSupplierDetail find(int sid, int cid) {
        TSupplierDetail supplier = getCache(joinKey(sid, cid), TSupplierDetail.class);
        if (null != supplier) {
            return supplier;
        }

        // 缓存没有就查询数据库
        TSupplierDetailExample example = new TSupplierDetailExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(cid);
        supplier = supplierDetailMapper.selectOneByExample(example);
        if (null != supplier) {
            setCache(joinKey(sid, cid), supplier);
        }
        return supplier;
    }

    public int total(int gid, int sid, String search) {
        if (null != search) {
            return 0;//return mySupplierDetailMapper.count(gid, sid, "%" + search + "%");
        } else {
            int total = getTotalCache(joinKey(gid, sid));
            if (0 != total) {
                return total;
            }
            TSupplierDetailExample example = new TSupplierDetailExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid);
            total = (int) supplierDetailMapper.countByExample(example);
            setTotalCache(joinKey(gid, sid), total);
            return total;
        }
    }

    public List<TSupplierDetail> pagination(int gid, int sid, int page, int limit, String search) {
        /*if (null != search) {
            return mySupplierDetailMapper.pagination((page - 1) * limit, limit, gid, sid, "%" + search + "%");
        } else {
            return mySupplierDetailMapper.pagination((page - 1) * limit, limit, gid, sid, null);
        }*/
        return null;
    }

    public boolean insert(TSupplierDetail row) {
        if (supplierDetailMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getSid()));
            return true;
        }
        return false;
    }

    public boolean update(TSupplierDetail row) {
        if (supplierDetailMapper.updateByPrimaryKey(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int cid) {
        TSupplierDetail supplier = find(sid, cid);
        if (null == supplier) {
            return false;
        }
        delCache(joinKey(supplier.getSid(), supplier.getCid()));
        delTotalCache(joinKey(supplier.getGid(), supplier.getSid()));
        return supplierDetailMapper.deleteByPrimaryKey(supplier.getId()) > 0;
    }
}
