package com.cxb.storehelperserver.controller.request.department;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class SetUserDepartmentValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "用户账号错误")
    private int uid;

    @Min(value = 1, message = "部门账号错误")
    private int did;
}
