package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TDepartment;
import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.repository.DepartmentRepository;
import com.cxb.storehelperserver.repository.UserRepository;
import com.cxb.storehelperserver.util.PageData;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.util.Permission.*;

/**
 * desc: 部门业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DepartmentService {
    @Resource
    private CheckService checkService;

    @Resource
    private DepartmentRepository departmentRepository;

    @Resource
    private UserRepository userRepository;

    public RestResult add(int id, TDepartment department) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_department)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 部门名重名检测
        if (departmentRepository.check(department.getName(), 0)) {
            return RestResult.fail("部门名称已存在");
        }

        if (!departmentRepository.insert(department)) {
            return RestResult.fail("添加部门信息失败");
        }
        return RestResult.ok();
    }

    public RestResult set(int id, TDepartment department) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_department)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 部门名重名检测
        if (departmentRepository.check(department.getName(), department.getId())) {
            return RestResult.fail("部门名称已存在");
        }

        if (!departmentRepository.update(department)) {
            return RestResult.fail("修改部门信息失败");
        }
        return RestResult.ok();
    }

    public RestResult del(int id, int cid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_department)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        TDepartment department = departmentRepository.find(cid);
        if (null == department) {
            return RestResult.fail("要删除的部门不存在");
        }

        // 存在子部门，不能删除
        if (departmentRepository.checkChildren(cid)) {
            return RestResult.fail("请先删除下级部门");
        }

        if (!departmentRepository.delete(cid)) {
            return RestResult.fail("删除部门信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getList(int id) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_department)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        val data = new HashMap<String, Object>();
        data.put("list", departmentRepository.all());
        return RestResult.ok(data);
    }

    public RestResult getTree(int id) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_department)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        List<TDepartment> categories = departmentRepository.all();
        if (null == categories) {
            return RestResult.fail("获取部门信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>(); // 结果树
        val tmps = new ArrayList<HashMap<String, Object>>(); // 临时表，用来查找父节点
        for (TDepartment c : categories) {
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

    public RestResult setUserDepartment(int id, int uid, int did) {
        // 权限校验
        if (!checkService.checkRolePermission(id, system_department)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 已存在就修改，不存在就新增
        TUser user = userRepository.find(uid);
        if (null == user) {
            return RestResult.fail("用户不存在");
        }
        user.setDid(did);
        if (!userRepository.update(user)) {
            return RestResult.fail("修改关联部门失败");
        }
        return RestResult.ok();
    }
}
