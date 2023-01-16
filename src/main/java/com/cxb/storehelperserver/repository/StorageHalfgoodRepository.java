package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageHalfgoodMapper;
import com.cxb.storehelperserver.model.TStorageHalfgood;
import com.cxb.storehelperserver.model.TStorageHalfgoodExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageHalfgoodMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储半成品仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class StorageHalfgoodRepository {
    @Resource
    private TStorageHalfgoodMapper storageHalfgoodMapper;

    @Resource
    private MyStorageHalfgoodMapper myStorageHalfgoodMapper;

    public TStorageHalfgood find(int sid, int id) {
        TStorageHalfgoodExample example = new TStorageHalfgoodExample();
        example.or().andSidEqualTo(sid).andHidEqualTo(id);
        return storageHalfgoodMapper.selectOneByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myStorageHalfgoodMapper.countByExample(sid, "%" + search + "%");
        } else {
            TStorageHalfgoodExample example = new TStorageHalfgoodExample();
            example.or().andSidEqualTo(sid);
            return (int) storageHalfgoodMapper.countByExample(example);
        }
    }

    public List<TStorageHalfgood> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageHalfgoodMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageHalfgoodMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageHalfgood row) {
        return storageHalfgoodMapper.insert(row) > 0;
    }

    public boolean update(TStorageHalfgood row) {
        return storageHalfgoodMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int id) {
        return storageHalfgoodMapper.deleteByPrimaryKey(id) > 0;
    }
}
