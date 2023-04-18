package com.cxb.storehelperserver.controller.request.market;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class SetMarketCommodityListValid implements IValid {
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

    @Size(min = 1, message = "商品id不能为空")
    private List<String> commoditys;

    @Size(min = 1, message = "商品单价不能为空")
    private List<BigDecimal> prices;

    @Size(min = 1, message = "商品件数不能为空")
    private List<Integer> values;

    @NotEmpty(message = "请输入查询日期")
    @Length(min = 10, max = 10, message = "查询日期格式错误")
    private String date;
}
