package com.cxb.storehelperserver.controller.request.halfgood;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class DelHalfgoodValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "半成品信息错误")
    private int hid;
}
