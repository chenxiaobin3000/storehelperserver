package com.cxb.storehelperserver.controller.request.product;

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
public class SetProcessValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "订单编号错误")
    private int oid;

    @Size(min = 1, message = "出库商品id不能为空")
    private List<Integer> commoditys;

    @Size(min = 1, message = "出库商品价格不能为空")
    private List<BigDecimal> prices;

    @Size(min = 1, message = "出库商品重量不能为空")
    private List<Integer> weights;

    @Size(min = 1, message = "商品规格不能为空")
    private List<String> norms;

    @Size(min = 1, message = "出库商品数量不能为空")
    private List<Integer> values;
}
