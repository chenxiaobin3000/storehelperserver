package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.repository.model.MyMarketCloud;
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
    private MarketCloudRepository marketCloudRepository;

    @Resource
    private GroupMarketRepository groupMarketRepository;

    @Resource
    private CloudRepository cloudRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    @Resource
    private DateUtil dateUtil;

    public RestResult addMarketAccount(int id, int gid, int mid, String account) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        if (!marketAccountRepository.insert(gid, mid, account)) {
            return RestResult.fail("添加账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setMarketAccount(int id, int gid, int mid, int aid, String account) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        val accounts = marketAccountRepository.find(gid, mid);
        for (TMarketAccount marketAccount : accounts) {
            if (marketAccount.getId().equals(aid)) {
                marketAccount.setAccount(account);
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
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        if (marketCloudRepository.check(aid)) {
            return RestResult.fail("存在关联云仓，不能删除账号");
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

    public RestResult setMarketCloud(int id, int gid, int mid, int cid, String account) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        if (null == cloudRepository.find(cid)) {
            return RestResult.fail("未查询到云仓信息");
        }
        val list = marketAccountRepository.find(gid, mid);
        for (TMarketAccount marketAccount : list) {
            if (marketAccount.getAccount().equals(account)) {
                if (!marketCloudRepository.insert(marketAccount.getId(), cid)) {
                    return RestResult.fail("修改关联云仓信息失败");
                }
                return RestResult.ok();
            }
        }
        return RestResult.fail("未查询到账号信息");
    }

    public RestResult delMarketCloud(int id, int gid, int mid, int cid) {
        // 验证公司
        String msg = checkService.checkGroup(id, gid);
        if (null != msg) {
            return RestResult.fail(msg);
        }
        if (!groupMarketRepository.check(gid, mid)) {
            return RestResult.fail("未开通平台权限");
        }
        if (!marketCloudRepository.delete(cid)) {
            return RestResult.fail("删除账号信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getMarketCloudList(int id, int page, int limit, String search) {
        // 验证公司
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int gid = group.getGid();

        int total = cloudRepository.total(gid, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val datas = new ArrayList<HashMap<String, Object>>();
        val list = cloudRepository.pagination(gid, page, limit, search);
        for (TCloud c : list) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("name", c.getName());
            datas.add(tmp);

            MyMarketCloud cloud = marketCloudRepository.find(c.getId());
            if (null != cloud) {
                tmp.put("mid", cloud.getMid());
                tmp.put("account", cloud.getAccount());
            }
        }
        return RestResult.ok(new PageData(total, datas));
    }
}
