package com.cxb.storehelperserver.controller.request.category;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class GetGroupCategoryValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;
}
