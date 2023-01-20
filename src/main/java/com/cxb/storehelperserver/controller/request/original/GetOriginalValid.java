package com.cxb.storehelperserver.controller.request.original;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class GetOriginalValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "原料信息错误")
    private int oid;
}
