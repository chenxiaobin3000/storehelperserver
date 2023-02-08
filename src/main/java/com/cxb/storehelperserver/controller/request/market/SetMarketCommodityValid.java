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
public class SetMarketCommodityValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "平台账号错误")
    private int mid;

    @Min(value = 1, message = "商品账号错误")
    private int cid;

    @NotEmpty(message = "请输入对接商品名称")
    @Length(min = 2, message = "对接商品名称长度不能小于2个字符")
    @Length(max = 16, message = "对接商品名称长度不能大于16个字符")
    private String name;

    @Min(value = 1, message = "对接商品预警价格错误")
    private BigDecimal price;
}
