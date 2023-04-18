package com.cxb.storehelperserver.controller.request.report;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/11
 */
@Data
public class getMarketReportValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 0, message = "平台账号错误")
    private int mid;

    @Min(value = 1, message = "查询周期错误")
    private int cycle;
}
