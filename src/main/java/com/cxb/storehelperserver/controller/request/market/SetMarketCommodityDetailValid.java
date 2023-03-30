package com.cxb.storehelperserver.controller.request.market;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class SetMarketCommodityDetailValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "云仓账号错误")
    private int sid;

    @Min(value = 1, message = "平台账号错误")
    private int aid;

    @Min(value = 0, message = "平台子账号错误")
    private int asid;

    @Min(value = 0, message = "明细账号错误")
    private int did;

    @Min(value = 1, message = "商品账号错误")
    private int cid;

    @Min(value = 1, message = "商品数量错误")
    private int value;

    @Min(value = 1, message = "商品价格错误")
    private BigDecimal price;

    @NotEmpty(message = "请输入查询日期")
    @Length(min = 10, max = 10, message = "查询日期格式错误")
    private String date;
}
