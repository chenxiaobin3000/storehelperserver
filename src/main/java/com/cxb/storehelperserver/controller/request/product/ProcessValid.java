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
public class ProcessValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 0, message = "仓库账号错误")
    private int sid;

    @NotEmpty(message = "请输入订单制单日期")
    @Length(min = 19, max = 19, message = "订单制单日期格式错误")
    private String date;

    @Min(value = 0, message = "一键审核错误")
    private int review;

    @Min(value = 0, message = "一键入库错误")
    private int storage;

    @Size(min = 1, message = "商品id不能为空")
    private List<Integer> commoditys;

    @Size(min = 1, message = "商品价格不能为空")
    private List<BigDecimal> prices;

    @Size(min = 1, message = "商品规格不能为空")
    private List<String> norms;

    @Size(min = 1, message = "商品重量不能为空")
    private List<Integer> weights;

    @Size(min = 1, message = "商品数量不能为空")
    private List<Integer> values;

    private List<Integer> attrs;
}