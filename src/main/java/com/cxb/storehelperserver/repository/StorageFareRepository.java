package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageFareMapper;
import com.cxb.storehelperserver.model.TStorageFare;
import com.cxb.storehelperserver.model.TStorageFareExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * desc: 仓储物流费用仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StorageFareRepository extends BaseRepository<List> {
    @Resource
    private TStorageFareMapper storageFareMapper;

    public StorageFareRepository() {
        init("storageFare::");
    }

    public List<TStorageFare> find(int oid) {
        List<TStorageFare> storageFares = getCache(oid, List.class);
        if (null != storageFares) {
            return storageFares;
        }

        // 缓存没有就查询数据库
        TStorageFareExample example = new TStorageFareExample();
        example.or().andOidEqualTo(oid);
        storageFares = storageFareMapper.selectByExample(example);
        if (null != storageFares) {
            setCache(oid, storageFares);
        }
        return storageFares;
    }

    public boolean insert(int oid, BigDecimal fare) {
        TStorageFare row = new TStorageFare();
        row.setOid(oid);
        row.setFare(fare);
        if (storageFareMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TStorageFare row) {
        if (storageFareMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStorageFare storageFare = storageFareMapper.selectByPrimaryKey(id);
        if (null == storageFare) {
            return false;
        }
        delCache(storageFare.getOid());
        return storageFareMapper.deleteByPrimaryKey(id) > 0;
    }
}
