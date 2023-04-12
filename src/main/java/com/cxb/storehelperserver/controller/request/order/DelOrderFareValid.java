package com.cxb.storehelperserver.controller.request.order;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class DelOrderFareValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "订单类型错误")
    private int otype;

    @Min(value = 1, message = "订单编号错误")
    private int oid;

    @Min(value = 1, message = "运费编号错误")
    private int fid;
}
