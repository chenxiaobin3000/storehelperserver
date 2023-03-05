package com.cxb.storehelperserver.controller.request.order;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/24
 */
@Data
public class GetCloudOrderValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "页面编号错误")
    private int page;

    @Min(value = 10, message = "页面数量错误")
    private int limit;

    @Min(value = 1, message = "类型错误")
    private int type;

    @Min(value = 1, message = "审核标志错误")
    private int review;

    @Length(max = 16, message = "搜索内容不能大于16个字符")
    private String search;
}
