package com.cxb.storehelperserver.controller.request.stock;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/24
 */
@Data
public class CountStockValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "仓库编号错误")
    private int sid;

    @NotEmpty(message = "请输入开始日期")
    @Length(min = 10, max = 10, message = "开始日期格式错误")
    private String date;
}
