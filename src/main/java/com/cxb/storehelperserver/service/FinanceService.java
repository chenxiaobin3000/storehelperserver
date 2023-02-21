package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TGroupDetail;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.GroupDetailRepository;
import com.cxb.storehelperserver.repository.GroupRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.HashMap;

import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction;

/**
 * desc: 财务业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class FinanceService {
    @Resource
    private CheckService checkService;

    @Resource
    private GroupRepository groupRepository;

    @Resource
    private GroupDetailRepository groupDetailRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult getFinance(int id, int page, int limit, int action) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int total = groupDetailRepository.total(group.getGid(), action);
        if (0 == total) {
            val data = new HashMap<String, Object>();
            data.put("total", 0);
            data.put("list", null);
            return RestResult.ok(data);
        }

        val list = groupDetailRepository.pagination(group.getGid(), page, limit, action);
        if (null == list) {
            return RestResult.fail("未查询到财务信息");
        }
        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", list);
        return RestResult.ok(data);
    }

    public boolean insertRecord(int id, int gid, FinanceAction action, int aid, BigDecimal value) {
        TGroup group = groupRepository.find(gid);
        if (null == group) {
            return false;
        }

        BigDecimal old = group.getMoney();
        TGroupDetail groupDetail = new TGroupDetail();
        groupDetail.setGid(gid);
        groupDetail.setUid(id);
        groupDetail.setOld(old);
        groupDetail.setValue(value);
        groupDetail.setAction(action.getValue());
        groupDetail.setAid(aid);
        if (!groupDetailRepository.insert(groupDetail)) {
            return false;
        }
        group.setMoney(old.add(value));
        log.info("finance:" + id + ", gid:" + gid + ", action:" + action + ", aid:" + aid + ", old:" + old + ", value:" + value);
        return groupRepository.update(group);
    }
}
