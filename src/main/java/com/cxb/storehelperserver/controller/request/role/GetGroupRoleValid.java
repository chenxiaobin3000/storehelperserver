package com.cxb.storehelperserver.controller.request.role;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/1
 */
@Data
public class GetGroupRoleValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;
}
