package com.cxb.storehelperserver.controller.request.stock;

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
 * date: 2023/1/24
 */
@Data
public class SetStockListValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "仓库账号错误")
    private int sid;

    @NotEmpty(message = "请输入导入日期")
    @Length(min = 19, max = 19, message = "导入日期格式错误")
    private String date;

    @Size(min = 1, message = "商品编号不能为空")
    private List<String> codes;

    @Size(min = 1, message = "商品名称不能为空")
    private List<String> names;

    @Size(min = 1, message = "商品价格不能为空")
    private List<BigDecimal> prices;

    @Size(min = 1, message = "商品重量不能为空")
    private List<Integer> weights;

    @Size(min = 1, message = "商品规格不能为空")
    private List<String> norms;

    @Size(min = 1, message = "商品件数不能为空")
    private List<Integer> values;
}
