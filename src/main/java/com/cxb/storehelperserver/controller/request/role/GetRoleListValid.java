package com.cxb.storehelperserver.controller.request.role;

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
public class GetRoleListValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Length(max = 16, message = "搜索内容不能大于16个字符")
    private String search;
}
