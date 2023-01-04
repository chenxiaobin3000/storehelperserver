package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TAttribute;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.AttributeRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
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
 * desc: 属性业务
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttributeService {
    @Resource
    private CheckService checkService;

    @Resource
    private AttributeRepository attributeRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addAttribute(int id, TAttribute attribute) {
        // 验证公司
        String msg = checkService.checkGroup(id, attribute.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 属性名重名检测
        if (attributeRepository.check(attribute.getGid(), attribute.getName())) {
            return RestResult.fail("属性名称已存在");
        }

        if (!attributeRepository.insert(attribute)) {
            return RestResult.fail("添加属性信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setAttribute(int id, TAttribute attribute) {
        // 验证公司
        String msg = checkService.checkGroup(id, attribute.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 属性名重名检测
        if (attributeRepository.check(attribute.getGid(), attribute.getName())) {
            return RestResult.fail("属性名称已存在");
        }

        if (!attributeRepository.update(attribute)) {
            return RestResult.fail("修改属性信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delAttribute(int id, int cid) {
        TAttribute attribute = attributeRepository.find(cid);
        if (null == attribute) {
            return RestResult.fail("要删除的属性不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, attribute.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!attributeRepository.delete(cid)) {
            return RestResult.fail("删除属性信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupAttribute(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        List<TAttribute> attributes = attributeRepository.findByGroup(group.getGid());
        if (null == attributes) {
            return RestResult.fail("获取属性信息异常");
        }

        val data = new HashMap<String, Object>();
        data.put("list", attributes);
        return RestResult.ok(data);
    }
}
