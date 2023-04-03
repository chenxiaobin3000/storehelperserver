package com.cxb.storehelperserver.controller.request.finance;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class AddLabelDetailValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "财务动作错误")
    private int action;

    @Min(value = 0, message = "财务附加信息错误")
    private int aid;

    @Min(value = 1, message = "财务价格错误")
    private BigDecimal value;

    @Length(max = 32, message = "备注字数不能超过32")
    private String remark;

    @NotEmpty(message = "请输入录入日期")
    @Length(min = 10, max = 10, message = "录入日期格式错误")
    private String date;

    @Min(value = 0, message = "财务操作账号错误")
    private int sub;
}
