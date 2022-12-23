package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TGroup;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.GroupRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * desc: 公司业务
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class GroupService {
    @Resource
    private GroupRepository groupRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addGroup(TGroup group) {
        boolean ret = groupRepository.insert(group);
        if (!ret) {
            RestResult.fail("添加公司信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setGroup(TGroup group) {
        boolean ret = groupRepository.update(group);
        if (!ret) {
            RestResult.fail("修改公司信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delGroup(int id) {
        boolean ret = groupRepository.delete(id);
        if (!ret) {
            RestResult.fail("删除公司信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getUserGroup(int uid) {
        TUserGroup userGroup = userGroupRepository.find(uid);
        if (null == userGroup) {
            return RestResult.fail("用户信息异常");
        }
        TGroup group = groupRepository.find(userGroup.getGid());
        if (null == group) {
            return RestResult.fail("公司信息异常");
        }
        return RestResult.ok(group);
    }

    public RestResult setUserGroup(int uid, int rid) {
        return RestResult.ok();
    }
}
