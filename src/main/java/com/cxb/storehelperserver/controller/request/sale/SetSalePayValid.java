package com.cxb.storehelperserver.controller.request.sale;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class SetSalePayValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "订单编号错误")
    private int oid;

    @Min(value = 0, message = "已收价格错误")
    private BigDecimal pay;
}
