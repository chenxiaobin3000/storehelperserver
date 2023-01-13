package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.repository.ReportRepository;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.TypeDefine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * desc: 报表业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ReportService {
    @Resource
    private ReportRepository reportRepository;

    public RestResult getAgreement(int id, int gid, TypeDefine.ReportCycleType type) {
        val data = new HashMap<String, Object>();
        data.put("list", reportRepository.find());
        return RestResult.ok(data);
    }

    public RestResult getFinance(int id, int gid, TypeDefine.ReportCycleType type) {
        val data = new HashMap<String, Object>();
        data.put("list", reportRepository.find());
        return RestResult.ok(data);
    }

    public RestResult getMarket(int id, int gid, TypeDefine.ReportCycleType type) {
        val data = new HashMap<String, Object>();
        data.put("list", reportRepository.find());
        return RestResult.ok(data);
    }

    public RestResult getProduct(int id, int gid, TypeDefine.ReportCycleType type) {
        val data = new HashMap<String, Object>();
        data.put("list", reportRepository.find());
        return RestResult.ok(data);
    }

    public RestResult getStorage(int id, int gid, TypeDefine.ReportCycleType type) {
        val data = new HashMap<String, Object>();
        data.put("list", reportRepository.find());
        return RestResult.ok(data);
    }
}
