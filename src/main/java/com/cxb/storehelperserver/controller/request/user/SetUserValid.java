package com.cxb.storehelperserver.controller.request.user;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/1
 */
@Data
public class SetUserValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "操作账号错误")
    private int uid;

    @NotEmpty(message = "请输入账号")
    @Length(min = 4, message = "账号长度不能小于4个字符")
    @Length(max = 16, message = "账号长度不能大于16个字符")
    private String name;

    @NotEmpty(message = "请输入手机号")
    @Length(min = 11, max = 11, message = "手机号格式错误")
    private String phone;
}
