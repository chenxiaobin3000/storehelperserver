package com.cxb.storehelperserver.controller.request.group;

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
public class AddGroupValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Length(min = 18, max = 18, message = "地区码错误")
    private String area;

    @Min(value = 1, message = "联系人错误")
    private int contact;

    @NotEmpty(message = "请输入公司名称")
    @Length(min = 4, message = "公司名称长度不能小于4个字符")
    @Length(max = 32, message = "公司名称长度不能大于32个字符")
    private String name;

    @NotEmpty(message = "请输入公司地址")
    @Length(min = 4, message = "公司地址长度不能小于4个字符")
    @Length(max = 32, message = "公司地址长度不能大于32个字符")
    private String address;
}
