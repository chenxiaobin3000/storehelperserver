package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.THalfgoodOriginalMapper;
import com.cxb.storehelperserver.model.THalfgoodOriginal;
import com.cxb.storehelperserver.model.THalfgoodOriginalExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 半成品原料仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class HalfgoodOriginalRepository extends BaseRepository<THalfgoodOriginal> {
    @Resource
    private THalfgoodOriginalMapper halfgoodOriginalMapper;

    public HalfgoodOriginalRepository() {
        init("halfOri::");
    }

    public THalfgoodOriginal find(int hid) {
        THalfgoodOriginal halfgoodOriginal = getCache(hid, THalfgoodOriginal.class);
        if (null != halfgoodOriginal) {
            return halfgoodOriginal;
        }

        // 缓存没有就查询数据库
        THalfgoodOriginalExample example = new THalfgoodOriginalExample();
        example.or().andHidEqualTo(hid);
        halfgoodOriginal = halfgoodOriginalMapper.selectOneByExample(example);
        if (null != halfgoodOriginal) {
            setCache(hid, halfgoodOriginal);
        }
        return halfgoodOriginal;
    }

    public boolean insert(THalfgoodOriginal row) {
        if (halfgoodOriginalMapper.insert(row) > 0) {
            setCache(row.getHid(), row);
            return true;
        }
        return false;
    }

    public boolean update(THalfgoodOriginal row) {
        if (halfgoodOriginalMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getHid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int gid, int hid) {
        delCache(joinKey(gid, hid));
        THalfgoodOriginalExample example = new THalfgoodOriginalExample();
        example.or().andHidEqualTo(hid);
        return halfgoodOriginalMapper.deleteByExample(example) > 0;
    }
}
