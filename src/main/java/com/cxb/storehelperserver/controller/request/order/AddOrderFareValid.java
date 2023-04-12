package com.cxb.storehelperserver.controller.request.order;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class AddOrderFareValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "订单类型错误")
    private int otype;

    @Min(value = 1, message = "订单编号错误")
    private int oid;

    @Length(min = 2, message = "物流长度不能小于2个字符")
    @Length(max = 16, message = "物流长度不能大于16个字符")
    private String ship;

    @Length(min = 2, message = "车牌长度不能小于2个字符")
    @Length(max = 10, message = "车牌长度不能大于10个字符")
    private String code;

    @Length(min = 11, max = 11, message = "手机号格式错误")
    private String phone;

    @Min(value = 1, message = "运费价格错误")
    private BigDecimal fare;

    @Length(max = 32, message = "备注长度不能大于32个字符")
    private String remark;
}
