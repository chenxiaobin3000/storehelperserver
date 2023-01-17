package com.cxb.storehelperserver.controller.request.user;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/3
 */
@Data
public class AddUserValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @NotEmpty(message = "请输入账号")
    @Length(min = 4, message = "账号长度不能小于4个字符")
    @Length(max = 16, message = "账号长度不能大于16个字符")
    private String account;

    @NotEmpty(message = "请输入手机号")
    @Length(min = 11, max = 11, message = "手机号格式错误")
    private String phone;

    @Min(value = 1, message = "角色信息错误")
    private int rid;
}
