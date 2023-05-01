package com.cxb.storehelperserver.controller.request.market;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class AddMarketAccountValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "平台账号错误")
    private int mid;

    @NotEmpty(message = "请输入账号")
    @Length(min = 2, message = "账号长度不能小于2个字符")
    @Length(max = 16, message = "账号长度不能大于16个字符")
    private String account;

    @Length(max = 16, message = "备注长度不能大于16个字符")
    private String remark;
}
