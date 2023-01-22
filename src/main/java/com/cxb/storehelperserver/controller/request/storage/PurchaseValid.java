package com.cxb.storehelperserver.controller.request.storage;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
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
public class PurchaseValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @NotEmpty(message = "请输入订单批次")
    @Length(min = 2, max = 16, message = "订单批次格式错误")
    private String batch;

    @Min(value = 1, message = "仓库账号错误")
    private int sid;

    @NotEmpty(message = "请输入订单制单日期")
    @Length(min = 10, max = 10, message = "订单制单日期格式错误")
    private String date;

    @Size(min = 1, message = "商品类型不能为空")
    private List<Integer> types;

    @Size(min = 1, message = "商品id不能为空")
    private List<Integer> commoditys;

    @Size(min = 1, message = "商品数量不能为空")
    private List<Integer> values;

    @Size(min = 1, message = "商品价格不能为空")
    private List<BigDecimal> prices;

    private List<Integer> attrs;
}
