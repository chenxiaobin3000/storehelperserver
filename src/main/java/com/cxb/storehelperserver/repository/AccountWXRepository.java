package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAccountWxMapper;
import com.cxb.storehelperserver.model.TAccountWx;
import com.cxb.storehelperserver.model.TAccountWxExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 用户微信仓库
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Repository
public class AccountWXRepository extends BaseRepository<TAccountWx> {
    @Resource
    private TAccountWxMapper accountWxMapper;

    public AccountWXRepository() {
        init("accountWX::");
    }

    public TAccountWx find(int wid) {
        TAccountWx tAccountWx = getCache(wid, TAccountWx.class);
        if (null != tAccountWx) {
            return tAccountWx;
        }

        // 缓存没有就查询数据库
        TAccountWxExample example = new TAccountWxExample();
        example.or().andWidEqualTo(wid);
        tAccountWx = accountWxMapper.selectOneByExample(example);
        if (null != tAccountWx) {
            setCache(wid, tAccountWx);
        }
        return tAccountWx;
    }

    public boolean insert(TAccountWx row) {
        int ret = accountWxMapper.insert(row);
        if (ret > 0) {
            setCache(row.getWid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TAccountWx row) {
        int ret = accountWxMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(row.getWid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(TAccountWx row) {
        int ret = accountWxMapper.deleteByPrimaryKey(row.getId());
        if (ret <= 0) {
            return false;
        }
        delCache(row.getWid());
        return true;
    }
}
