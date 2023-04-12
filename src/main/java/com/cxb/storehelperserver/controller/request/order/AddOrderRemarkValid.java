package com.cxb.storehelperserver.controller.request.order;

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
public class AddOrderRemarkValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "订单类型错误")
    private int otype;

    @Min(value = 1, message = "订单编号错误")
    private int oid;

    @Length(min = 2, message = "备注长度不能小于2个字符")
    @Length(max = 32, message = "备注长度不能大于32个字符")
    private String remark;
}
