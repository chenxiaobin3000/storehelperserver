package com.cxb.storehelperserver.controller.request.stock;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/24
 */
@Data
public class GetCloudWeekValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 0, message = "仓库账号错误")
    private int sid;

    @NotEmpty(message = "请输入查询日期")
    @Length(min = 10, max = 10, message = "查询日期格式错误")
    private String date;

    @Min(value = 1, message = "页面编号错误")
    private int page;

    @Min(value = 10, message = "页面数量错误")
    private int limit;

    @Length(max = 16, message = "搜索内容不能大于16个字符")
    private String search;
}
