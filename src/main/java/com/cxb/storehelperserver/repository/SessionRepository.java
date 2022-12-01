package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSessionMapper;
import com.cxb.storehelperserver.model.TSession;
import com.cxb.storehelperserver.model.TSessionExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 会话仓库
 * auth: cxb
 * date: 2022/12/1
 */
@Slf4j
@Repository
public class SessionRepository extends BaseRepository<Integer> {
    @Resource
    private TSessionMapper sessionMapper;

    public SessionRepository() {
        init("session::");
    }

    public Integer find(String key) {
        Integer uid = getCache(key, Integer.class);
        if (null != uid) {
            return uid;
        }

        // 缓存没有就查询数据库
        TSessionExample example = new TSessionExample();
        example.or().andKeyEqualTo(key);
        val ret = sessionMapper.selectByExample(example);
        if (ret.isEmpty()) {
            return 0;
        }
        return ret.get(0).getUid();
    }

    public boolean insert(TSession row) {
        int ret = sessionMapper.insert(row);
        if (ret > 0) {
            setCache(row.getKey(), row.getUid());
            return true;
        }
        return false;
    }

    public boolean update(TSession row) {
        int ret = sessionMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(row.getKey(), row.getUid());
            return true;
        }
        return false;
    }
}
