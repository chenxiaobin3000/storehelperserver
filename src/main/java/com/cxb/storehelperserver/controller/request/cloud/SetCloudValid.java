package com.cxb.storehelperserver.controller.request.cloud;

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
public class SetCloudValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "云仓账号错误")
    private int sid;

    @Length(min = 18, max = 18, message = "地区码错误")
    private String area;

    @Min(value = 1, message = "联系人错误")
    private int contact;

    @NotEmpty(message = "请输入云仓名称")
    @Length(min = 4, message = "云仓名称长度不能小于4个字符")
    @Length(max = 32, message = "云仓名称长度不能大于32个字符")
    private String name;

    @NotEmpty(message = "请输入云仓地址")
    @Length(min = 4, message = "云仓地址长度不能小于4个字符")
    @Length(max = 32, message = "云仓地址长度不能大于32个字符")
    private String address;
}
