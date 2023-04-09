package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStandardStorageMapper;
import com.cxb.storehelperserver.model.TStandard;
import com.cxb.storehelperserver.model.TStandardStorage;
import com.cxb.storehelperserver.model.TStandardStorageExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageCommodityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 标品仓库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class StandardStorageRepository extends BaseRepository<List> {
    @Resource
    private TStandardStorageMapper standardStorageMapper;

    @Resource
    private MyStorageCommodityMapper myStorageCommodityMapper;

    public StandardStorageRepository() {
        init("stanStorage::");
    }

    public List<TStandardStorage> find(int cid) {
        List<TStandardStorage> standardStorages = getCache(cid, List.class);
        if (null != standardStorages) {
            return standardStorages;
        }

        // 缓存没有就查询数据库
        TStandardStorageExample example = new TStandardStorageExample();
        example.or().andCidEqualTo(cid);
        standardStorages = standardStorageMapper.selectByExample(example);
        if (null != standardStorages) {
            setCache(cid, standardStorages);
        }
        return standardStorages;
    }

    public List<TStandardStorage> findBySid(int sid) {
        TStandardStorageExample example = new TStandardStorageExample();
        example.or().andSidEqualTo(sid);
        return standardStorageMapper.selectByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myStorageCommodityMapper.count_standard(sid, "%" + search + "%");
        } else {
            return myStorageCommodityMapper.count_standard(sid, null);
        }
    }

    public List<TStandard> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageCommodityMapper.pagination_standard((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageCommodityMapper.pagination_standard((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean update(int cid, List<Integer> sids) {
        delete(cid);
        TStandardStorage row = new TStandardStorage();
        row.setCid(cid);
        for (Integer sid : sids) {
            row.setId(0);
            row.setSid(sid);
            if (standardStorageMapper.insert(row) <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean delete(int cid) {
        delCache(cid);
        TStandardStorageExample example = new TStandardStorageExample();
        example.or().andCidEqualTo(cid);
        return standardStorageMapper.deleteByExample(example) > 0;
    }
}
