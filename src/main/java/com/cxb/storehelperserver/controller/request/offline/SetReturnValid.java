package com.cxb.storehelperserver.controller.request.offline;

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
public class SetReturnValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "订单编号错误")
    private int oid;

    @Size(min = 1, message = "商品id不能为空")
    private List<Integer> commoditys;

    @Size(min = 1, message = "商品总价不能为空")
    private List<BigDecimal> prices;

    @Size(min = 1, message = "商品重量不能为空")
    private List<Integer> weights;

    @Size(min = 1, message = "商品数量不能为空")
    private List<Integer> values;
}
