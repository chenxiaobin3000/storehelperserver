package com.cxb.storehelperserver.service;

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
    public String create(int uid) {
        String key = randomUUID();
        TSession session = new TSession();
        session.setUid(uid);
        session.setKey(key);
        if (sessionRepository.insert(session)) {
            return key;
        } else {
            return null;
        }
    }

    /**
     * desc: 校验会话id，返回用户id
     */
    public int check(String key) {
        return sessionRepository.find(key);
    }

    /**
     * desc: 生成会话 id
     */
    public String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
