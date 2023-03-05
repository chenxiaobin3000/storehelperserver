package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TGroupDetail;
import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.GroupDetailRepository;
import com.cxb.storehelperserver.repository.GroupRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.repository.UserRepository;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction;
import static com.cxb.storehelperserver.util.TypeDefine.FinanceAction.*;

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

    @Resource
    private UserRepository userRepository;

    @Resource
    private DateUtil dateUtil;

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
        if (null == list || list.isEmpty()) {
            return RestResult.fail("未查询到财务信息");
        }

        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val tmps = new ArrayList<HashMap<String, Object>>();
        for (TGroupDetail detail : list) {
            val tmp = new HashMap<String, Object>();
            tmp.put("value", detail.getValue());
            tmp.put("now", detail.getOld().add(detail.getValue()));
            tmp.put("time", dateFormat.format(detail.getCdate()));

            TUser user = userRepository.find(detail.getUid());
            if (null == user) {
                tmp.put("user", detail.getUid());
            } else {
                tmp.put("user", user.getName());
            }

            explainAction(FinanceAction.valueOf(detail.getAction()), tmp);
            tmps.add(tmp);
        }

        val data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("list", tmps);
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
        groupDetail.setCdate(new Date());
        if (!groupDetailRepository.insert(groupDetail)) {
            return false;
        }
        group.setMoney(old.add(value));
        log.info("finance:" + id + ", gid:" + gid + ", action:" + action + ", aid:" + aid + ", old:" + old + ", value:" + value);
        return groupRepository.update(group);
    }

    private void explainAction(FinanceAction action, HashMap<String, Object> data) {
        switch (action) {
            case FINANCE_PURCHASE_PAY:
                data.put("action", "采购进货");
                break;
            case FINANCE_PURCHASE_FARE:
                data.put("action", "采购进货运费");
                break;
            case FINANCE_PURCHASE_RET:
                data.put("action", "采购退货");
                break;
            case FINANCE_PURCHASE_FARE2:
                data.put("action", "采购退货运费");
                break;

            case FINANCE_STORAGE_MGR:
                data.put("action", "仓储管理费");
                break;
            case FINANCE_STORAGE_FARE:
                data.put("action", "仓储调度运费");
                break;
            case FINANCE_STORAGE_RET:
                data.put("action", "采购退货");
                break;
            case FINANCE_STORAGE_FARE2:
                data.put("action", "采购退货运费");
                break;

            case FINANCE_PRODUCT_WRAP:
                data.put("action", "包装费");
                break;
            case FINANCE_PRODUCT_MAN:
                data.put("action", "人工费用");
                break;
            case FINANCE_PRODUCT_OUT:
                data.put("action", "外厂费用");
                break;

            case FINANCE_AGREEMENT_FARE:
                data.put("action", "履约发货物流");
                break;
            case FINANCE_AGREEMENT_FARE2:
                data.put("action", "履约退款物流");
                break;

            case FINANCE_CLOUD_RET:
                data.put("action", "云仓退货");
                break;
            case FINANCE_CLOUD_FARE:
                data.put("action", "云仓退货运费");
                break;

            case FINANCE_MARKET_SALE:
                data.put("action", "销售平台");
                break;

            case FINANCE_GROUP_OTHER:
                data.put("action", "经营费用");
                break;

            default:
                data.put("action", "未知操作");
                break;
        }
    }
}
