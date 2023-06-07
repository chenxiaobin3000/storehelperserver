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
public class SetAccountCommodityValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "平台账号错误")
    private int aid;

    @Size(min = 1, message = "商品id不能为空")
    private List<Integer> commoditys;

    @Size(min = 1, message = "商品编号不能为空")
    private List<String> codes;

    @Size(min = 1, message = "商品名称不能为空")
    private List<String> names;

    @Size(min = 1, message = "商品备注不能为空")
    private List<String> remarks;

    @Size(min = 1, message = "商品预警价格不能为空")
    private List<BigDecimal> alarms;
}
