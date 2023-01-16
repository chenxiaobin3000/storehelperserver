package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageStandardMapper;
import com.cxb.storehelperserver.model.TStorageStandard;
import com.cxb.storehelperserver.model.TStorageStandardExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageStandardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储标品仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StorageStandardRepository {
    @Resource
    private TStorageStandardMapper storageStandardMapper;

    @Resource
    private MyStorageStandardMapper myStorageStandardMapper;

    public TStorageStandard find(int sid, int id) {
        TStorageStandardExample example = new TStorageStandardExample();
        example.or().andSidEqualTo(sid).andStidEqualTo(id);
        return storageStandardMapper.selectOneByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myStorageStandardMapper.countByExample(sid, "%" + search + "%");
        } else {
            TStorageStandardExample example = new TStorageStandardExample();
            example.or().andSidEqualTo(sid);
            return (int) storageStandardMapper.countByExample(example);
        }
    }

    public List<TStorageStandard> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageStandardMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageStandardMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageStandard row) {
        return storageStandardMapper.insert(row) > 0;
    }

    public boolean update(TStorageStandard row) {
        return storageStandardMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int id) {
        return storageStandardMapper.deleteByPrimaryKey(id) > 0;
    }
}
