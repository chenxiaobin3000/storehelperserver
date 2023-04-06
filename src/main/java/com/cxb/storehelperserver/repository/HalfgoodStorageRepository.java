package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.THalfgoodStorageMapper;
import com.cxb.storehelperserver.model.THalfgoodStorage;
import com.cxb.storehelperserver.model.THalfgoodStorageExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 半成品仓库关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class HalfgoodStorageRepository extends BaseRepository<List> {
    @Resource
    private THalfgoodStorageMapper halfgoodStorageMapper;

    public HalfgoodStorageRepository() {
        init("halfStorage::");
    }

    public List<THalfgoodStorage> find(int cid) {
        List<THalfgoodStorage> halfgoodStorages = getCache(cid, List.class);
        if (null != halfgoodStorages) {
            return halfgoodStorages;
        }

        // 缓存没有就查询数据库
        THalfgoodStorageExample example = new THalfgoodStorageExample();
        example.or().andCidEqualTo(cid);
        halfgoodStorages = halfgoodStorageMapper.selectByExample(example);
        if (null != halfgoodStorages) {
            setCache(cid, halfgoodStorages);
        }
        return halfgoodStorages;
    }

    public List<THalfgoodStorage> findBySid(int sid) {
        THalfgoodStorageExample example = new THalfgoodStorageExample();
        example.or().andSidEqualTo(sid);
        return halfgoodStorageMapper.selectByExample(example);
    }

    public boolean update(int cid, List<Integer> sids) {
        delete(cid);
        THalfgoodStorage row = new THalfgoodStorage();
        row.setCid(cid);
        for (Integer sid : sids) {
            row.setId(0);
            row.setSid(sid);
            if (halfgoodStorageMapper.insert(row) <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean delete(int cid) {
        delCache(cid);
        THalfgoodStorageExample example = new THalfgoodStorageExample();
        example.or().andCidEqualTo(cid);
        return halfgoodStorageMapper.deleteByExample(example) > 0;
    }
}
