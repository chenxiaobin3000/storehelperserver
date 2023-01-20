package com.cxb.storehelperserver.controller.request.destroy;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class GetDestroyValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "废品信息错误")
    private int did;
}
