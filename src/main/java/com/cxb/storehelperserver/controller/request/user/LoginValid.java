package com.cxb.storehelperserver.controller.request.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/1
 */
@Getter
@Setter
public class LoginValid {
    @NotEmpty(message="账号不能为空")
    private String account;
}
