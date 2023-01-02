package com.cxb.storehelperserver.controller.request.account;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/1
 */
@Data
public class SetPasswordValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @NotEmpty(message = "请输入旧密码")
    @Length(min = 32, max = 32, message = "旧密码格式错误")
    private String oldpassword;

    @NotEmpty(message = "请输入新密码")
    @Length(min = 32, max = 32, message = "新密码格式错误")
    private String newpassword;
}
