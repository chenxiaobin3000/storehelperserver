package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalHalfgoodMapper;
import com.cxb.storehelperserver.model.TOriginalHalfgood;
import com.cxb.storehelperserver.model.TOriginalHalfgoodExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料半成品仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OriginalHalfgoodRepository extends BaseRepository<TOriginalHalfgood> {
    @Resource
    private TOriginalHalfgoodMapper originalHalfgoodMapper;

    public OriginalHalfgoodRepository() {
        init("oriHalf::");
    }

    public TOriginalHalfgood find(int gid, int hid) {
        TOriginalHalfgood originalHalfgood = getCache(getKey(gid, hid), TOriginalHalfgood.class);
        if (null != originalHalfgood) {
            return originalHalfgood;
        }

        // 缓存没有就查询数据库
        TOriginalHalfgoodExample example = new TOriginalHalfgoodExample();
        example.or().andGidEqualTo(gid).andHidEqualTo(hid);
        originalHalfgood = originalHalfgoodMapper.selectOneByExample(example);
        if (null != originalHalfgood) {
            setCache(getKey(gid, hid), originalHalfgood);
        }
        return originalHalfgood;
    }

    public boolean insert(TOriginalHalfgood row) {
        if (originalHalfgoodMapper.insert(row) > 0) {
            setCache(getKey(row.getGid(), row.getHid()), row);
            return true;
        }
        return false;
    }

    public boolean update(TOriginalHalfgood row) {
        if (originalHalfgoodMapper.updateByPrimaryKey(row) > 0) {
            setCache(getKey(row.getGid(), row.getHid()), row);
            return true;
        }
        return false;
    }

    public boolean delete(int gid, int hid) {
        delCache(getKey(gid, hid));
        TOriginalHalfgoodExample example = new TOriginalHalfgoodExample();
        example.or().andGidEqualTo(gid).andHidEqualTo(hid);
        return originalHalfgoodMapper.deleteByExample(example) > 0;
    }

    private String getKey(int gid, int hid) {
        return String.valueOf(gid) + "::" + String.valueOf(hid);
    }
}
