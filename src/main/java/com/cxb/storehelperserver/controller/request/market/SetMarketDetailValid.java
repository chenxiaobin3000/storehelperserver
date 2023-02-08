package com.cxb.storehelperserver.controller.request.market;

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
public class SetMarketDetailValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "明细账号错误")
    private int did;

    @Min(value = 1, message = "商品数量错误")
    private int value;

    @Min(value = 1, message = "商品价格错误")
    private BigDecimal price;
}
