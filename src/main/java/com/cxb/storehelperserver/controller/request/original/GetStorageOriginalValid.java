package com.cxb.storehelperserver.controller.request.original;

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
public class GetStorageOriginalValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "仓库账号错误")
    private int sid;

    @Min(value = 1, message = "页面编号错误")
    private int page;

    @Min(value = 10, message = "页面数量错误")
    private int limit;

    @Length(max = 16, message = "搜索内容不能大于16个字符")
    private String search;
}
