package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TCategory;
import com.cxb.storehelperserver.repository.CategoryRepository;
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

import static com.cxb.storehelperserver.util.Permission.commodity_category;

/**
 * desc: 品类业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CategoryService {
    @Resource
    private CheckService checkService;

    @Resource
    private CategoryRepository categoryRepository;

    public RestResult add(int id, TCategory category) {
        // 权限校验
        if (!checkService.checkRolePermission(id, commodity_category)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 品类名重名检测
        if (categoryRepository.check(category.getName(), 0)) {
            return RestResult.fail("品类名称已存在");
        }

        if (!categoryRepository.insert(category)) {
            return RestResult.fail("添加品类信息失败");
        }
        return RestResult.ok();
    }

    public RestResult set(int id, TCategory category) {
        // 权限校验
        if (!checkService.checkRolePermission(id, commodity_category)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 品类名重名检测
        if (categoryRepository.check(category.getName(), category.getId())) {
            return RestResult.fail("品类名称已存在");
        }

        if (!categoryRepository.update(category)) {
            return RestResult.fail("修改品类信息失败");
        }
        return RestResult.ok();
    }

    public RestResult del(int id, int cid) {
        // 权限校验
        if (!checkService.checkRolePermission(id, commodity_category)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        TCategory category = categoryRepository.find(cid);
        if (null == category) {
            return RestResult.fail("要删除的品类不存在");
        }

        // 存在子品类，不能删除
        if (categoryRepository.checkChildren(cid)) {
            return RestResult.fail("请先删除下级品类");
        }

        if (!categoryRepository.delete(cid)) {
            return RestResult.fail("删除品类信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getList(int id) {
        // 权限校验
        if (!checkService.checkRolePermission(id, commodity_category)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        val data = new HashMap<String, Object>();
        data.put("list", categoryRepository.findAll());
        return RestResult.ok(data);
    }

    public RestResult getTree(int id) {
        // 权限校验
        if (!checkService.checkRolePermission(id, commodity_category)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        List<TCategory> categories = categoryRepository.findAll();
        if (null == categories) {
            return RestResult.fail("获取品类信息失败");
        }

        val datas = new ArrayList<HashMap<String, Object>>(); // 结果树
        val tmps = new ArrayList<HashMap<String, Object>>(); // 临时表，用来查找父节点
        for (TCategory c : categories) {
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
