package com.cxb.storehelperserver.controller.request.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/1
 */
@Data
public class RegisterValid {
    @NotEmpty(message = "请输入账号")
    @Length(min = 4, message = "账号长度不能小于4个字符")
    @Length(max = 16, message = "账号长度不能大于16个字符")
    private String account;

    @NotEmpty(message = "请输入密码")
    @Length(min = 32, max = 32, message = "密码格式错误")
    private String password;

    @NotEmpty(message = "请输入手机号")
    @Length(min = 11, max = 11, message = "手机号格式错误")
    private String phone;
}
