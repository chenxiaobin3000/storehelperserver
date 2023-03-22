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
public class AddCloudValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Length(min = 18, max = 18, message = "地区码错误")
    private String area;

    @NotEmpty(message = "请输入云仓名称")
    @Length(min = 4, message = "云仓名称长度不能小于4个字符")
    @Length(max = 32, message = "云仓名称长度不能大于32个字符")
    private String name;

    @NotEmpty(message = "请输入联系人名称")
    @Length(min = 2, message = "联系人名称长度不能小于2个字符")
    @Length(max = 8, message = "联系人名称长度不能大于8个字符")
    private String contact;

    @NotEmpty(message = "请输入手机号")
    @Length(min = 11, max = 11, message = "手机号格式错误")
    private String phone;

    @NotEmpty(message = "请输入云仓地址")
    @Length(min = 4, message = "云仓地址长度不能小于4个字符")
    @Length(max = 32, message = "云仓地址长度不能大于32个字符")
    private String address;
}
