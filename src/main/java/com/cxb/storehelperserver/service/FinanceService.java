package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.repository.GroupDetailRepository;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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
    private GroupDetailRepository groupDetailRepository;

    public RestResult getFinance(int id, int page, int limit, int type, String search) {
        return RestResult.ok();
    }

    public boolean insertRecord(int id, FinanceAction action) {
        return true;
    }
}
