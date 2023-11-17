package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TUser;
import com.cxb.storehelperserver.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 校验业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CheckService {
    @Resource
    private RolePermissionRepository rolePermissionRepository;

    @Resource
    private UserRepository userRepository;

    public boolean checkRolePermission(int uid, int permission) {
        TUser user = userRepository.find(uid);
        if (null == user) {
            return false;
        }
        List<Integer> permissions = rolePermissionRepository.find(user.getRid());
        if (null != permissions) {
            for (Integer p1 : permissions) {
                if (p1.equals(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}
