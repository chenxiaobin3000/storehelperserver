package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSoInOriginalMapper;
import com.cxb.storehelperserver.model.TSoInOriginal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料入库原料仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SoInOriginalRepository extends BaseRepository<TSoInOriginal> {
    @Resource
    private TSoInOriginalMapper soInOriginalMapper;

    public SoInOriginalRepository() {
        init("soInOri::");
    }

    public TSoInOriginal find(int id) {
        TSoInOriginal soInOriginal = getCache(id, TSoInOriginal.class);
        if (null != soInOriginal) {
            return soInOriginal;
        }

        // 缓存没有就查询数据库
        soInOriginal = soInOriginalMapper.selectByPrimaryKey(id);
        if (null != soInOriginal) {
            setCache(id, soInOriginal);
        }
        return soInOriginal;
    }

    public boolean insert(TSoInOriginal row) {
        if (soInOriginalMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSoInOriginal row) {
        if (soInOriginalMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return soInOriginalMapper.deleteByPrimaryKey(id) > 0;
    }
}
