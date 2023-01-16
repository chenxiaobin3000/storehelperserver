package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageOriginalMapper;
import com.cxb.storehelperserver.model.TStorageOriginal;
import com.cxb.storehelperserver.model.TStorageOriginalExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageOriginalMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储原料仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StorageOriginalRepository {
    @Resource
    private TStorageOriginalMapper storageOriginalMapper;

    @Resource
    private MyStorageOriginalMapper myStorageOriginalMapper;

    public TStorageOriginal find(int sid, int id) {
        TStorageOriginalExample example = new TStorageOriginalExample();
        example.or().andSidEqualTo(sid).andOidEqualTo(id);
        return storageOriginalMapper.selectOneByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myStorageOriginalMapper.countByExample(sid, "%" + search + "%");
        } else {
            TStorageOriginalExample example = new TStorageOriginalExample();
            example.or().andSidEqualTo(sid);
            return (int) storageOriginalMapper.countByExample(example);
        }
    }

    public List<TStorageOriginal> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageOriginalMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageOriginalMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageOriginal row) {
        return storageOriginalMapper.insert(row) > 0;
    }

    public boolean update(TStorageOriginal row) {
        return storageOriginalMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int id) {
        return storageOriginalMapper.deleteByPrimaryKey(id) > 0;
    }
}
