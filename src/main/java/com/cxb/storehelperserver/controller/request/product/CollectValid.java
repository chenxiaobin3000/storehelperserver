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
public class CollectValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "仓库账号错误")
    private int sid;

    @NotEmpty(message = "请输入订单制单日期")
    @Length(min = 19, max = 19, message = "订单制单日期格式错误")
    private String date;

    @Size(min = 1, message = "出库商品id不能为空")
    private List<Integer> commoditys;

    @Size(min = 1, message = "出库商品价格不能为空")
    private List<BigDecimal> prices;

    @Size(min = 1, message = "出库商品重量不能为空")
    private List<Integer> weights;

    @Size(min = 1, message = "出库商品数量不能为空")
    private List<Integer> values;

    @Size(min = 1, message = "入库商品id不能为空")
    private List<Integer> commoditys2;

    @Size(min = 1, message = "入库商品价格不能为空")
    private List<BigDecimal> prices2;

    @Size(min = 1, message = "入库商品重量不能为空")
    private List<Integer> weights2;

    @Size(min = 1, message = "入库商品数量不能为空")
    private List<Integer> values2;

    private List<Integer> commoditys3;

    private List<BigDecimal> prices3;

    private List<Integer> weights3;

    private List<Integer> values3;

    private List<Integer> attrs;
}
