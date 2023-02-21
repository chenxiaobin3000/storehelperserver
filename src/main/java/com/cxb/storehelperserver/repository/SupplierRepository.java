package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSupplierMapper;
import com.cxb.storehelperserver.model.TSupplier;
import com.cxb.storehelperserver.model.TSupplierExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class SupplierRepository extends BaseRepository<TSupplier> {
    @Resource
    private TSupplierMapper supplierMapper;

    public SupplierRepository() {
        init("supplier::");
    }

    public TSupplier find(int id) {
        TSupplier supplier = getCache(id, TSupplier.class);
        if (null != supplier) {
            return supplier;
        }

        // 缓存没有就查询数据库
        supplier = supplierMapper.selectByPrimaryKey(id);
        if (null != supplier) {
            setCache(id, supplier);
        }
        return supplier;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TSupplierExample example = new TSupplierExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) supplierMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TSupplierExample example = new TSupplierExample();
            example.or().andGidEqualTo(gid);
            total = (int) supplierMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TSupplier> pagination(int gid, int page, int limit, String search) {
        TSupplierExample example = new TSupplierExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return supplierMapper.selectByExample(example);
    }

    /*
     * desc: 判断公司是否存在仓库
     */
    public boolean check(int gid, String name, int id) {
        TSupplierExample example = new TSupplierExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != supplierMapper.selectOneByExample(example);
        } else {
            TSupplier supplier = supplierMapper.selectOneByExample(example);
            return null != supplier && !supplier.getId().equals(id);
        }
    }

    public boolean insert(TSupplier row) {
        if (supplierMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TSupplier row) {
        if (supplierMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TSupplier supplier = find(id);
        if (null == supplier) {
            return false;
        }
        delCache(id);
        delTotalCache(supplier.getGid());
        return supplierMapper.deleteByPrimaryKey(id) > 0;
    }
}
