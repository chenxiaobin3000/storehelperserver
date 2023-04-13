package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.service.model.PageData;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private FinanceLabelRepository financeLabelRepository;

    @Resource
    private FinanceDetailRepository financeDetailRepository;

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
            return RestResult.ok(new PageData());
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
        return RestResult.ok(new PageData(total, tmps));
    }

    /*public boolean insertRecord(int id, int gid, FinanceAction action, int aid, BigDecimal value) {
        TGroupDetail groupDetail = new TGroupDetail();
        groupDetail.setGid(gid);
        groupDetail.setUid(id);
        groupDetail.setValue(value);
        groupDetail.setAction(action.getValue());
        groupDetail.setAid(aid);
        groupDetail.setCdate(new Date());
        return groupDetailRepository.insert(groupDetail);
    }*/

    public RestResult getLabelList(int id, int page, int limit, int action, Date date) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }
        int total = financeDetailRepository.total(group.getGid(), action, date);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }

        val list = financeDetailRepository.pagination(group.getGid(), page, limit, action, date);
        if (null == list || list.isEmpty()) {
            return RestResult.fail("未查询到财务信息");
        }

        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val tmps = new ArrayList<HashMap<String, Object>>();
        for (TFinanceDetail detail : list) {
            val tmp = new HashMap<String, Object>();
            tmp.put("value", detail.getValue());
            tmp.put("aid", detail.getAid());
            tmp.put("remark", detail.getRemark());
            tmp.put("time", dateFormat.format(detail.getCdate()).substring(0, 10));

            TUser user = userRepository.find(detail.getUid());
            if (null == user) {
                tmp.put("user", detail.getUid());
            } else {
                tmp.put("user", user.getName());
            }

            TFinanceLabel label = financeLabelRepository.find(detail.getAction());
            if (null != label) {
                tmp.put("action", label.getName());
            }
            tmps.add(tmp);
        }
        return RestResult.ok(new PageData(total, tmps));
    }

    public RestResult insertLabelDetail(int id, int gid, int action, int aid, BigDecimal value, String remark, Date date, int sub) {
        if (sub > 0) {
            value = value.negate();
        }
        TFinanceDetail detail = new TFinanceDetail();
        detail.setGid(gid);
        detail.setUid(id);
        detail.setValue(value);
        detail.setAction(action);
        detail.setAid(aid);
        detail.setRemark(remark);
        detail.setCdate(date);
        if (!financeDetailRepository.insert(detail)) {
            return RestResult.fail("添加财务记录失败");
        }
        return RestResult.ok();
    }

    private void explainAction(FinanceAction action, HashMap<String, Object> data) {
        switch (action) {
            case FINANCE_PURCHASE_PAY:
                data.put("action", "采购仓储进货");
                break;
            case FINANCE_PURCHASE_FARE:
                data.put("action", "采购仓储进货运费");
                break;
            case FINANCE_PURCHASE_RET:
                data.put("action", "采购仓储退货");
                break;
            case FINANCE_PURCHASE_FARE2:
                data.put("action", "采购仓储退货运费");
                break;

            case FINANCE_STORAGE_MGR:
                data.put("action", "仓储管理费");
                break;
            case FINANCE_STORAGE_FARE:
                data.put("action", "仓储调度运费");
                break;

            case FINANCE_PRODUCT_PAY:
                data.put("action", "生产费用");
                break;

            case FINANCE_AGREEMENT_FARE:
                data.put("action", "履约发货物流");
                break;
            case FINANCE_AGREEMENT_FARE2:
                data.put("action", "履约退款物流");
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
