package com.cxb.storehelperserver.controller.request.finance;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class GetFinanceValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "页面编号错误")
    private int page;

    @Min(value = 10, message = "页面数量错误")
    private int limit;

    @Min(value = 0, message = "财务动作错误")
    private int action;
}
