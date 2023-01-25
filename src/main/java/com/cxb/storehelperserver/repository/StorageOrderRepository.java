package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageOrderMapper;
import com.cxb.storehelperserver.model.TStorageOrder;
import com.cxb.storehelperserver.model.TStorageOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 进货出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class StorageOrderRepository extends BaseRepository<TStorageOrder> {
    @Resource
    private TStorageOrderMapper storageOrderMapper;

    public StorageOrderRepository() {
        init("sOrder::");
    }

    public TStorageOrder find(int id) {
        TStorageOrder storageOrder = getCache(id, TStorageOrder.class);
        if (null != storageOrder) {
            return storageOrder;
        }

        // 缓存没有就查询数据库
        storageOrder = storageOrderMapper.selectByPrimaryKey(id);
        if (null != storageOrder) {
            setCache(id, storageOrder);
        }
        return storageOrder;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TStorageOrderExample example = new TStorageOrderExample();
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
            return (int) storageOrderMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TStorageOrderExample example = new TStorageOrderExample();
            example.or().andGidEqualTo(gid);
            total = (int) storageOrderMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TStorageOrder> pagination(int gid, int page, int limit, String search) {
        TStorageOrderExample example = new TStorageOrderExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andBatchLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return storageOrderMapper.selectByExample(example);
    }

    public boolean insert(TStorageOrder row) {
        if (storageOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TStorageOrder row) {
        if (storageOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return storageOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
