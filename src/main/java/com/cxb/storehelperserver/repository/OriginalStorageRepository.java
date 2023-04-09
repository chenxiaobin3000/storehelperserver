package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalStorageMapper;
import com.cxb.storehelperserver.model.TOriginal;
import com.cxb.storehelperserver.model.TOriginalStorage;
import com.cxb.storehelperserver.model.TOriginalStorageExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageCommodityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 废料仓库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OriginalStorageRepository extends BaseRepository<List> {
    @Resource
    private TOriginalStorageMapper originalStorageMapper;

    @Resource
    private MyStorageCommodityMapper myStorageCommodityMapper;

    public OriginalStorageRepository() {
        init("oriStorage::");
    }

    public List<TOriginalStorage> find(int cid) {
        List<TOriginalStorage> originalStorages = getCache(cid, List.class);
        if (null != originalStorages) {
            return originalStorages;
        }

        // 缓存没有就查询数据库
        TOriginalStorageExample example = new TOriginalStorageExample();
        example.or().andCidEqualTo(cid);
        originalStorages = originalStorageMapper.selectByExample(example);
        if (null != originalStorages) {
            setCache(cid, originalStorages);
        }
        return originalStorages;
    }

    public List<TOriginalStorage> findBySid(int sid) {
        TOriginalStorageExample example = new TOriginalStorageExample();
        example.or().andSidEqualTo(sid);
        return originalStorageMapper.selectByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myStorageCommodityMapper.count_original(sid, "%" + search + "%");
        } else {
            return myStorageCommodityMapper.count_original(sid, null);
        }
    }

    public List<TOriginal> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageCommodityMapper.pagination_original((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageCommodityMapper.pagination_original((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean update(int cid, List<Integer> sids) {
        delete(cid);
        TOriginalStorage row = new TOriginalStorage();
        row.setCid(cid);
        for (Integer sid : sids) {
            row.setId(0);
            row.setSid(sid);
            if (originalStorageMapper.insert(row) <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TOriginalStorageExample example = new TOriginalStorageExample();
        example.or().andCidEqualTo(cid);
        return originalStorageMapper.deleteByExample(example) > 0;
    }
}
