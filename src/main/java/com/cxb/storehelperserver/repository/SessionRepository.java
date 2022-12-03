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

    /**
     * desc: 仅用于 logout 不用缓存
     */
    public TSession find(int id) {
        TSessionExample example = new TSessionExample();
        example.or().andUidEqualTo(id);
        return sessionMapper.selectOneByExample(example);
    }

    /**
     * desc: 对应 delete 操作
     */
    public int findByIdFromCache(int uid) {
        Integer id = getCache(String.valueOf(uid), Integer.class);
        if (null != id) {
            return id;
        }
        return 0;
    }

    public Integer findByToken(String key) {
        Integer uid = getCache(key, Integer.class);
        if (null != uid) {
            return uid;
        }

        // 缓存没有就查询数据库
        TSessionExample example = new TSessionExample();
        example.or().andTokenEqualTo(key);
        val ret = sessionMapper.selectByExample(example);
        if (ret.isEmpty()) {
            return 0;
        }
        return ret.get(0).getUid();
    }

    public boolean insert(TSession row) {
        int ret = sessionMapper.insert(row);
        if (ret > 0) {
            setCache(row.getToken(), row.getUid());
            return true;
        }
        return false;
    }

    /**
     * desc: 修改 delete 生成的 null 数据
     */
    public boolean update(TSession row) {
        delCache(String.valueOf(row.getUid()));
        int ret = sessionMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(row.getToken(), row.getUid());
            return true;
        }
        return false;
    }

    public boolean delete(TSession row) {
        delCache(row.getToken());
        row.setToken("null");
        int ret = sessionMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            // 改为用 uid 创建缓存，在登陆时再删除
            setCache(String.valueOf(row.getUid()), row.getId());
            return true;
        }
        return false;
    }
}
