package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAccountMapper;
import com.cxb.storehelperserver.model.TAccount;
import com.cxb.storehelperserver.model.TAccountExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

    /**
     * desc: 仅用于 logout 使用，不用缓存
     */
    public TAccount find(int id) {
        TAccountExample example = new TAccountExample();
        example.or().andUidEqualTo(id);
        return accountMapper.selectOneByExample(example);
    }

    public TAccount findByAccount(String account) {
        TAccount tAccount = getCache(account, TAccount.class);
        if (null != tAccount) {
            return tAccount;
        }

        // 缓存没有就查询数据库
        TAccountExample example = new TAccountExample();
        example.or().andAccountEqualTo(account);
        val rets = accountMapper.selectByExample(example);
        if (rets.isEmpty()) {
            return null;
        }
        return rets.get(0);
    }

    public boolean insert(TAccount row) {
        int ret = accountMapper.insert(row);
        if (ret > 0) {
            setCache(row.getAccount(), row);
            return true;
        }
        return false;
    }

    public boolean update(TAccount row) {
        int ret = accountMapper.updateByPrimaryKey(row);
        if (ret > 0) {
            setCache(row.getAccount(), row);
            return true;
        }
        return false;
    }
}
