package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.repository.SupplierRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * desc: 供应商业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SupplierService {
    @Resource
    private SupplierRepository supplierRepository;

    public RestResult getPermission() {
        val data = new HashMap<String, Object>();
        data.put("list", supplierRepository.find());
        return RestResult.ok(data);
    }
}
