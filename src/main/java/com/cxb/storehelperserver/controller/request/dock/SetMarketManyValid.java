package com.cxb.storehelperserver.controller.request.dock;

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
public class SetMarketManyValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "平台账号错误")
    private int mid;

    @Min(value = 1, message = "主账号信息错误")
    private int aid;

    @Min(value = 1, message = "子账号信息错误")
    private int sub;

    @NotEmpty(message = "请输入账号")
    @Length(min = 4, message = "账号长度不能小于4个字符")
    @Length(max = 16, message = "账号长度不能大于16个字符")
    private String account;

    @Length(max = 16, message = "备注长度不能大于16个字符")
    private String remark;
}
