package com.cxb.storehelperserver.controller.request.user;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/1
 */
@Getter
@Setter
public class RegisterValid {
    @NotEmpty(message = "账号不能为空")
    private String account;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 32, max = 32, message = "密码格式错误")
    private String password;
}
