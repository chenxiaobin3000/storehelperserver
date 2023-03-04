package com.cxb.storehelperserver.controller.request.cloud;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class LossValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "仓库账号错误")
    private int sid;

    @Min(value = 1, message = "采购单号错误")
    private int pid;

    @NotEmpty(message = "请输入订单制单日期")
    @Length(min = 19, max = 19, message = "订单制单日期格式错误")
    private String date;

    @Size(min = 1, message = "商品类型不能为空")
    private List<Integer> types;

    @Size(min = 1, message = "商品id不能为空")
    private List<Integer> commoditys;

    @Size(min = 1, message = "商品数量不能为空")
    private List<Integer> values;

    private List<Integer> attrs;
}