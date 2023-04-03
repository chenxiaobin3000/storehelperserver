package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TFinanceLabel;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.FinanceLabelRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.service.model.PageData;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * desc: 财务类目业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class FinanceLabelService {
    @Resource
    private CheckService checkService;

    @Resource
    private FinanceLabelRepository financeLabelRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addabel(int id, TFinanceLabel financeLabel) {
        // 验证公司
        String msg = checkService.checkGroup(id, financeLabel.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 类目名重名检测
        if (financeLabelRepository.check(financeLabel.getGid(), financeLabel.getName(), 0)) {
            return RestResult.fail("类目名称已存在");
        }

        if (!financeLabelRepository.insert(financeLabel)) {
            return RestResult.fail("添加类目信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setLabel(int id, TFinanceLabel financeLabel) {
        // 验证公司
        String msg = checkService.checkGroup(id, financeLabel.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 类目名重名检测
        if (financeLabelRepository.check(financeLabel.getGid(), financeLabel.getName(), financeLabel.getId())) {
            return RestResult.fail("类目名称已存在");
        }

        if (!financeLabelRepository.update(financeLabel)) {
            return RestResult.fail("修改类目信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delLabel(int id, int cid) {
        TFinanceLabel financeLabel = financeLabelRepository.find(cid);
        if (null == financeLabel) {
            return RestResult.fail("要删除的类目不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, financeLabel.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 存在子类目，不能删除
        if (financeLabelRepository.checkChildren(cid)) {
            return RestResult.fail("请先删除下级类目");
        }

        if (!financeLabelRepository.delete(cid)) {
            return RestResult.fail("删除类目信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupLabelList(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        val data = new HashMap<String, Object>();
        data.put("list", financeLabelRepository.findByGroup(group.getGid()));
        return RestResult.ok(data);
    }

    public RestResult getGroupLabelTree(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息失败");
        }

        List<TFinanceLabel> categories = financeLabelRepository.findByGroup(group.getGid());
        if (null == categories) {
            return RestResult.fail("获取类目信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>(); // 结果树
        val tmps = new ArrayList<HashMap<String, Object>>(); // 临时表，用来查找父节点
        for (TFinanceLabel c : categories) {
            val tmp = new HashMap<String, Object>();
            tmp.put("id", c.getId());
            tmp.put("label", c.getName());
            tmp.put("parent", c.getParent());
            tmp.put("level", c.getLevel());
            tmp.put("children", null);
            tmps.add(tmp); // 查询表插入所有节点

            Integer pid = c.getParent();
            if (pid.equals(0)) {
                datas.add(tmp); // 结果表只插入根节点
                continue;
            }

            // 从父类列表查找父类目，并插入
            for (HashMap<String, Object> p : tmps) {
                if (p.get("id").equals(pid)) {
                    List<HashMap<String, Object>> children = (List<HashMap<String, Object>>) p.get("children");
                    if (null == children) {
                        children = new ArrayList<>();
                        p.put("children", children);
                    }
                    children.add(tmp);
                    break;
                }
            }
        }
        return RestResult.ok(new PageData(0, datas));
    }
}
