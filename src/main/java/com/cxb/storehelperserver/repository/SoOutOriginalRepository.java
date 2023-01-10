package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSoOutOriginalMapper;
import com.cxb.storehelperserver.model.TSoOutOriginal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料出库原料仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SoOutOriginalRepository extends BaseRepository<TSoOutOriginal> {
    @Resource
    private TSoOutOriginalMapper soOutOriginalMapper;

    public SoOutOriginalRepository() {
        init("soOutOri::");
    }

    public TSoOutOriginal find(int id) {
        TSoOutOriginal soOutOriginal = getCache(id, TSoOutOriginal.class);
        if (null != soOutOriginal) {
            return soOutOriginal;
        }

        // 缓存没有就查询数据库
        soOutOriginal = soOutOriginalMapper.selectByPrimaryKey(id);
        if (null != soOutOriginal) {
            setCache(id, soOutOriginal);
        }
        return soOutOriginal;
    }

    public boolean insert(TSoOutOriginal row) {
        if (soOutOriginalMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSoOutOriginal row) {
        if (soOutOriginalMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return soOutOriginalMapper.deleteByPrimaryKey(id) > 0;
    }
}
