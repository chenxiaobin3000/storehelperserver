package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TAccount;
import com.cxb.storehelperserver.model.TSession;
import com.cxb.storehelperserver.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * desc: 会话业务
 * auth: cxb
 * date: 2022/11/30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SessionService {
    @Resource
    private SessionRepository sessionRepository;

    /**
     * desc: 无论之前是否存在，都强制生成新的会话
     */
    public String create(TAccount account) {
        String key = randomUUID();
        TSession session = sessionRepository.find(account.getUid());
        // 已存在就修改，否则添加
        if (null != session) {
            String oldKey = session.getToken();
            session.setToken(key);
            if (sessionRepository.update(session, oldKey)) {
                return key;
            }
        } else {
            if (sessionRepository.insert(account.getUid(), key)) {
                return key;
            }
        }
        return null;
    }

    /**
     * desc: 校验会话id，返回用户id
     */
    public int check(String key) {
        return sessionRepository.findByToken(key);
    }

    /**
     * desc:
     */
    public boolean delete(int id) {
        TSession session = sessionRepository.find(id);
        if (null == session) {
            return false;
        }
        return sessionRepository.delete(session);
    }

    /**
     * desc: 生成会话 id
     */
    private String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
