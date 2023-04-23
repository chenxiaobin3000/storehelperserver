package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyMarketStorage;
import com.cxb.storehelperserver.service.model.PageData;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * desc: 市场对接业务
 * auth: cxb
 * date: 2023/2/7
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DockService {
    @Resource
    private CheckService checkService;

    @Resource
    private MarketAccountRepository marketAccountRepository;

    @Resource
    private MarketManyRepository marketManyRepository;

    @Resource
    private MarketStorageRepository marketStorageRepository;

    @Resource
    private MarketCommodityRepository marketCommodityRepository;

    @Resource
    private GroupMarketRepository groupMarketRepository;

    @Resource
    private StorageRepository storageRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult addMarketAccount(int id, int gid, int mid, String account, String remark) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        if (!marketAccountRepository.insert(gid, mid, account, remark)) {
            return RestResult.fail("添加账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setMarketAccount(int id, int gid, int mid, int aid, String account, String remark) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        val accounts = marketAccountRepository.find(gid, mid);
        for (TMarketAccount marketAccount : accounts) {
            if (marketAccount.getId().equals(aid)) {
                marketAccount.setAccount(account);
                marketAccount.setRemark(remark);
                if (!marketAccountRepository.update(marketAccount)) {
                    return RestResult.fail("修改对接商品信息失败");
                }
                return RestResult.ok();
            }
        }
        return RestResult.fail("未查询到账号信息");
    }

    public RestResult delMarketAccount(int id, int gid, int mid, int aid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (marketManyRepository.check(aid)) {
            return RestResult.fail("存在关联子账号，不能删除账号");
        }
        if (marketStorageRepository.check(aid)) {
            return RestResult.fail("存在关联仓库，不能删除账号");
        }
        if (!marketAccountRepository.delete(aid)) {
            return RestResult.fail("删除账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketAccountList(int id, int gid, int mid, int page, int limit) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        int total = marketAccountRepository.total(gid, mid);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketAccountRepository.pagination(gid, mid, page, limit);
        if (null == list) {
            return RestResult.fail("未查询到账号信息");
        }
        val datas = new HashMap<>();
        datas.put("total", total);
        datas.put("list", list);
        return RestResult.ok(datas);
    }

    public RestResult getMarketAllAccount(int id, int gid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        int total = marketAccountRepository.total(gid, 0);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketAccountRepository.pagination(gid, 0, 1, total);
        if (null == list) {
            return RestResult.fail("未查询到账号信息");
        }
        val datas = new HashMap<>();
        datas.put("total", total);
        datas.put("list", list);
        return RestResult.ok(datas);
    }

    public RestResult getMarketStorageAccount(int id, int gid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        MyMarketStorage storage = marketStorageRepository.find(cid);
        if (null == storage) {
            return RestResult.fail("未查询到账号信息");
        }
        return RestResult.ok(storage);
    }

    public RestResult getMarketSubAccount(int id, int gid, int aid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        val list = marketManyRepository.findByAid(aid);
        if (null == list) {
            return RestResult.fail("未查询到账号信息");
        }
        val datas = new HashMap<>();
        datas.put("total", 0);
        datas.put("list", list);
        return RestResult.ok(datas);
    }

    public RestResult addMarketMany(int id, int gid, int mid, int aid, String account, String remark) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        if (!marketManyRepository.insert(gid, mid, aid, account, remark)) {
            return RestResult.fail("添加账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setMarketMany(int id, int gid, int mid, int aid, int sub, String account, String remark) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        TMarketMany marketMany = marketManyRepository.find(sub);
        if (null == marketMany) {
            return RestResult.fail("未查询到账号信息");
        }
        marketMany.setMid(mid);
        marketMany.setAid(aid);
        marketMany.setAccount(account);
        marketMany.setRemark(remark);
        if (!marketManyRepository.update(marketMany)) {
            return RestResult.fail("修改对接商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delMarketMany(int id, int gid, int sub) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (marketCommodityRepository.checkByAsid(sub)) {
            return RestResult.fail("存在关联商品，不能删除账号");
        }
        if (!marketManyRepository.delete(sub)) {
            return RestResult.fail("删除账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketManyList(int id, int gid, int page, int limit) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        int total = marketManyRepository.total(gid);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = marketManyRepository.pagination(gid, page, limit);
        if (null == list) {
            return RestResult.fail("未查询到账号信息");
        }
        val datas = new ArrayList<HashMap<String, Object>>();
        for (TMarketMany many : list) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", many.getId());
            tmp.put("aid", many.getAid());
            tmp.put("smid", many.getMid());
            tmp.put("account", many.getAccount());
            tmp.put("remark", many.getRemark());
            datas.add(tmp);

            TMarketAccount account = marketAccountRepository.find(many.getAid());
            if (null != account) {
                tmp.put("mmid", account.getMid());
                tmp.put("maccount", account.getAccount());
                tmp.put("mremark", account.getRemark());
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }

    public RestResult setMarketStorage(int id, int gid, int aid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (null == storageRepository.find(cid)) {
            return RestResult.fail("未查询到仓库信息");
        }
        if (null == marketAccountRepository.find(aid)) {
            return RestResult.fail("未查询到主账号信息");
        }
        MyMarketStorage storage = marketStorageRepository.find(cid);
        if (null == storage) {
            if (!marketStorageRepository.insert(aid, cid)) {
                return RestResult.fail("修改关联仓库信息失败");
            }
        } else {
            TMarketStorage s = new TMarketStorage();
            s.setId(storage.getId());
            s.setAid(aid);
            s.setCid(cid);
            if (!marketStorageRepository.update(s)) {
                return RestResult.fail("修改关联仓库信息失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult delMarketStorage(int id, int gid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        MyMarketStorage marketStorage = marketStorageRepository.find(cid);
        if (null == marketStorage) {
            return RestResult.fail("账号信息不存在");
        }
        if (marketCommodityRepository.checkByAid(marketStorage.getAid())) {
            return RestResult.fail("存在关联商品，不能删除账号");
        }
        if (!marketStorageRepository.delete(cid)) {
            return RestResult.fail("删除账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketStorageList(int id, int page, int limit, String search) {
        // 验证公司
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        int total = storageRepository.total(gid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        val list = storageRepository.pagination(gid, page, limit, search);
        for (TStorage s : list) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", s.getId());
            tmp.put("name", s.getName());
            datas.add(tmp);

            MyMarketStorage storage = marketStorageRepository.find(s.getId());
            if (null != storage) {
                tmp.put("mid", storage.getMid());
                tmp.put("aid", storage.getAid());
                tmp.put("account", storage.getAccount());
                tmp.put("remark", storage.getRemark());
                tmp.put("sub", marketManyRepository.findByAid(storage.getAid()));
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }
}
