package com.cxb.storehelperserver.controller.request.rolemp;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class SetUserRoleMpValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    // 0为删除用户的小程序角色
    @Min(value = 0, message = "角色账号错误")
    private int rid;

    @Min(value = 1, message = "用户账号错误")
    private int uid;
}
