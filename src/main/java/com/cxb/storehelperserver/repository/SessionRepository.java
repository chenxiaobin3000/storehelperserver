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
public class SessionRepository extends BaseRepository<TSession> {
    @Resource
    private TSessionMapper sessionMapper;

    public SessionRepository() {
        init("session::");
    }

    public TSession find(int id) {
        TSession session = getCache(id, TSession.class);
        if (null != session) {
            return session;
        }

        // 缓存没有就查询数据库
        TSessionExample example = new TSessionExample();
        example.or().andUidEqualTo(id);
        session = sessionMapper.selectOneByExample(example);
        if (null != session) {
            setCache(session.getToken(), session);
            setCache(session.getUid(), session);
        }
        return session;
    }

    public Integer findByToken(String key) {
        TSession session = getCache(key, TSession.class);
        if (null != session) {
            return session.getUid();
        }

        // 缓存没有就查询数据库
        TSessionExample example = new TSessionExample();
        example.or().andTokenEqualTo(key);
        session = sessionMapper.selectOneByExample(example);
        if (null == session) {
            return 0;
        }
        setCache(session.getToken(), session);
        setCache(session.getUid(), session);
        return session.getUid();
    }

    public boolean insert(TSession row) {
        int ret = sessionMapper.insert(row);
        if (ret > 0) {
            setCache(row.getToken(), row);
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSession row, String oldKey) {
        delCache(oldKey);
        int ret = sessionMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(row.getToken(), row);
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(TSession row) {
        delCache(row.getToken());
        delCache(row.getUid());
        int ret = sessionMapper.deleteByPrimaryKey(row.getUid());
        if (ret > 0) {
            return true;
        }
        return false;
    }
}
