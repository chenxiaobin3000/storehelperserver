package com.cxb.storehelperserver.controller.request.storage;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class GetGroupAllStorageValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;
}
