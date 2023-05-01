package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOfflineReturnMapper;
import com.cxb.storehelperserver.model.TOfflineReturn;
import com.cxb.storehelperserver.model.TOfflineReturnExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 线下销售退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OfflineReturnRepository extends BaseRepository<TOfflineReturn> {
    @Resource
    private TOfflineReturnMapper offlineReturnMapper;

    public OfflineReturnRepository() {
        init("offlineReturn::");
    }

    public TOfflineReturn find(int oid) {
        TOfflineReturn offlineReturn = getCache(oid, TOfflineReturn.class);
        if (null != offlineReturn) {
            return offlineReturn;
        }

        // 缓存没有就查询数据库
        TOfflineReturnExample example = new TOfflineReturnExample();
        example.or().andOidEqualTo(oid);
        offlineReturn = offlineReturnMapper.selectOneByExample(example);
        if (null != offlineReturn) {
            setCache(oid, offlineReturn);
        }
        return offlineReturn;
    }

    public boolean insert(int oid, int sid) {
        TOfflineReturn row = new TOfflineReturn();
        row.setOid(oid);
        row.setSid(sid);
        if (offlineReturnMapper.insert(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TOfflineReturn row) {
        if (offlineReturnMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int sid) {
        delCache(oid);
        TOfflineReturnExample example = new TOfflineReturnExample();
        example.or().andOidEqualTo(oid).andSidEqualTo(sid);
        return offlineReturnMapper.deleteByExample(example) > 0;
    }
}
