package com.cxb.storehelperserver.controller.request.attributeTemplate;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class GetGroupAttrTemplateValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 0, message = "模板id错误")
    private int atid;
}
