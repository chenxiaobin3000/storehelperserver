package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.repository.ConfigPermissionRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * desc: 配置业务
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConfigService {
    @Resource
    private ConfigPermissionRepository configPermissionRepository;

    public RestResult getPermission() {
        val data = new HashMap<String, Object>();
        data.put("list", configPermissionRepository.all());
        return RestResult.ok(data);
    }
}
