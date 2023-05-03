package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOfflineStorageMapper;
import com.cxb.storehelperserver.model.TOfflineStorage;
import com.cxb.storehelperserver.model.TOfflineStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 线下销售退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OfflineStorageRepository extends BaseRepository<TOfflineStorage> {
    @Resource
    private TOfflineStorageMapper offlineStorageMapper;

    public OfflineStorageRepository() {
        init("offlineStorage::");
    }

    public TOfflineStorage find(int oid) {
        TOfflineStorage offlineStorage = getCache(oid, TOfflineStorage.class);
        if (null != offlineStorage) {
            return offlineStorage;
        }

        // 缓存没有就查询数据库
        TOfflineStorageExample example = new TOfflineStorageExample();
        example.or().andOidEqualTo(oid);
        offlineStorage = offlineStorageMapper.selectOneByExample(example);
        if (null != offlineStorage) {
            setCache(oid, offlineStorage);
        }
        return offlineStorage;
    }

    public TOfflineStorage findBySid(int sid) {
        TOfflineStorageExample example = new TOfflineStorageExample();
        example.or().andSidEqualTo(sid);
        return offlineStorageMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int sid) {
        TOfflineStorage row = new TOfflineStorage();
        row.setOid(oid);
        row.setSid(sid);
        if (offlineStorageMapper.insert(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TOfflineStorage row) {
        if (offlineStorageMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int sid) {
        delCache(oid);
        TOfflineStorageExample example = new TOfflineStorageExample();
        example.or().andOidEqualTo(oid).andSidEqualTo(sid);
        return offlineStorageMapper.deleteByExample(example) > 0;
    }
}
