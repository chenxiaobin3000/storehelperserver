package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.repository.FinanceRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

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
    private FinanceRepository financeRepository;

    public RestResult getPermission() {
        val data = new HashMap<String, Object>();
        data.put("list", financeRepository.find());
        return RestResult.ok(data);
    }
}
