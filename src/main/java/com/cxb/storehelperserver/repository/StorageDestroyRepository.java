package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageDestroyMapper;
import com.cxb.storehelperserver.model.TStorageDestroy;
import com.cxb.storehelperserver.model.TStorageDestroyExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageDestroyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储废料仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StorageDestroyRepository {
    @Resource
    private TStorageDestroyMapper storageDestroyMapper;

    @Resource
    private MyStorageDestroyMapper myStorageDestroyMapper;

    public TStorageDestroy find(int sid, int id) {
        TStorageDestroyExample example = new TStorageDestroyExample();
        example.or().andSidEqualTo(sid).andDidEqualTo(id);
        return storageDestroyMapper.selectOneByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myStorageDestroyMapper.countByExample(sid, "%" + search + "%");
        } else {
            TStorageDestroyExample example = new TStorageDestroyExample();
            example.or().andSidEqualTo(sid);
            return (int) storageDestroyMapper.countByExample(example);
        }
    }

    public List<TStorageDestroy> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageDestroyMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageDestroyMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageDestroy row) {
        return storageDestroyMapper.insert(row) > 0;
    }

    public boolean update(TStorageDestroy row) {
        return storageDestroyMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int id) {
        return storageDestroyMapper.deleteByPrimaryKey(id) > 0;
    }
}
