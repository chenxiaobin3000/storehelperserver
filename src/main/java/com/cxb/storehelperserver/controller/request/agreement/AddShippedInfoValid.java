package com.cxb.storehelperserver.controller.request.agreement;

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
public class AddShippedInfoValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "订单编号错误")
    private int oid;

    @Min(value = 1, message = "运费价格错误")
    private BigDecimal fare;

    @Length(max = 32, message = "备注字数不能超过32")
    private String remark;
}
