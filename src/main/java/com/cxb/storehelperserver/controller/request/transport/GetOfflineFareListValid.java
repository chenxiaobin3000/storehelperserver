package com.cxb.storehelperserver.controller.request.transport;

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
public class GetOfflineFareListValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 0, message = "平台账号错误")
    private int aid;

    @Min(value = 0, message = "仓库账号错误")
    private int sid;

    @Min(value = 1, message = "类型错误")
    private int type;

    @Min(value = 1, message = "审核标志错误")
    private int review;

    @Min(value = 0, message = "完成状态错误")
    private int complete;

    @Length(max = 10, message = "订单制单日期格式错误")
    private String date;

    @Min(value = 1, message = "页面编号错误")
    private int page;

    @Min(value = 10, message = "页面数量错误")
    private int limit;

    @Length(max = 16, message = "搜索内容不能大于16个字符")
    private String search;
}
