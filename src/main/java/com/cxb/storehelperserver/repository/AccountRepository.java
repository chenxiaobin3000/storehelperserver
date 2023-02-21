package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAccountMapper;
import com.cxb.storehelperserver.model.TAccount;
import com.cxb.storehelperserver.model.TAccountExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 账号仓库
 * auth: cxb
 * date: 2022/12/1
 */
@Slf4j
@Repository
public class AccountRepository extends BaseRepository<TAccount> {
    @Resource
    private TAccountMapper accountMapper;

    public AccountRepository() {
        init("account::");
    }

    public TAccount find(String account) {
        TAccount tAccount = getCache(account, TAccount.class);
        if (null != tAccount) {
            return tAccount;
        }

        // 缓存没有就查询数据库
        TAccountExample example = new TAccountExample();
        example.or().andAccountEqualTo(account);
        tAccount = accountMapper.selectOneByExample(example);
        if (null != tAccount) {
            setCache(account, tAccount);
        }
        return tAccount;
    }

    public TAccount find(int id) {
        TAccount tAccount = getCache(id, TAccount.class);
        if (null != tAccount) {
            return tAccount;
        }

        // 缓存没有就查询数据库
        TAccountExample example = new TAccountExample();
        example.or().andUidEqualTo(id);
        tAccount = accountMapper.selectOneByExample(example);
        if (null != tAccount) {
            setCache(id, tAccount);
        }
        return tAccount;
    }

    public boolean insert(String account, String password, int uid) {
        TAccount row = new TAccount();
        row.setAccount(account);
        row.setPassword(password);
        row.setUid(uid);
        if (accountMapper.insert(row) > 0) {
            setCache(row.getAccount(), row);
            return true;
        }
        return false;
    }

    public boolean updatePassword(int uid, String password) {
        TAccount account = find(uid);
        if (null == account) {
            return false;
        }
        account.setPassword(password);
        if (accountMapper.updateByPrimaryKey(account) > 0) {
            setCache(account.getAccount(), account);
            setCache(uid, account);
            return true;
        }
        return false;
    }
}
