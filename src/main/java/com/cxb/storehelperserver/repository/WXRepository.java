package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TWxMapper;
import com.cxb.storehelperserver.model.TWx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 公司仓库
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Repository
public class WXRepository extends BaseRepository<TWx> {
    @Resource
    private TWxMapper wxMapper;

    public WXRepository() {
        init("wx::");
    }

    public TWx find(int id) {
        TWx tWx = getCache(id, TWx.class);
        if (null != tWx) {
            return tWx;
        }

        // 缓存没有就查询数据库
        tWx = wxMapper.selectByPrimaryKey(id);
        if (null != tWx) {
            setCache(id, tWx);
        }
        return tWx;
    }

    public boolean insert(TWx row) {
        int ret = wxMapper.insert(row);
        if (ret > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TWx row) {
        int ret = wxMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        int ret = wxMapper.deleteByPrimaryKey(id);
        if (ret <= 0) {
            return false;
        }
        delCache(id);
        return true;
    }
}
