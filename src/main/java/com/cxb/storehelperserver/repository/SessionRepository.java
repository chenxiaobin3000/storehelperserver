package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSessionMapper;
import com.cxb.storehelperserver.model.TSession;
import com.cxb.storehelperserver.model.TSessionExample;
import lombok.extern.slf4j.Slf4j;
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
            setCache(id, session);
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
        setCache(key, session);
        setCache(session.getUid(), session);
        return session.getUid();
    }

    public boolean insert(int uid, String key) {
        TSession row = new TSession();
        row.setUid(uid);
        row.setToken(key);
        if (sessionMapper.insert(row) > 0) {
            setCache(row.getToken(), row);
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSession row, String oldKey) {
        delCache(oldKey);
        if (sessionMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getToken(), row);
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(TSession row) {
        delCache(row.getToken());
        delCache(row.getUid());
        return sessionMapper.deleteByPrimaryKey(row.getId()) > 0;
    }
}
