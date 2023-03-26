package com.cxb.storehelperserver.controller.request.market;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class GetMarketStandardDetailValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "页面编号错误")
    private int page;

    @Min(value = 10, message = "页面数量错误")
    private int limit;

    @Min(value = 1, message = "云仓账号错误")
    private int sid;

    @Min(value = 0, message = "平台账号错误")
    private int mid;

    @NotEmpty(message = "请输入查询日期")
    @Length(min = 10, max = 10, message = "查询日期格式错误")
    private String date;

    @Length(max = 16, message = "搜索内容不能大于16个字符")
    private String search;
}
