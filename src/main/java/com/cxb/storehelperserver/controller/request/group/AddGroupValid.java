package com.cxb.storehelperserver.controller.request.group;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

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

    @NotEmpty(message = "请输入账号")
    @Length(min = 4, message = "账号长度不能小于4个字符")
    @Length(max = 16, message = "账号长度不能大于16个字符")
    private String account;

    @NotEmpty(message = "请输入手机号")
    @Length(min = 11, max = 11, message = "手机号格式错误")
    private String phone;

    @NotEmpty(message = "请输入公司名称")
    @Length(min = 4, message = "公司名称长度不能小于4个字符")
    @Length(max = 32, message = "公司名称长度不能大于32个字符")
    private String name;

    @NotEmpty(message = "请输入公司地址")
    @Length(min = 4, message = "公司地址长度不能小于4个字符")
    @Length(max = 32, message = "公司地址长度不能大于32个字符")
    private String address;

    @NotEmpty(message = "请选择平台账号")
    @Size(min = 1, message = "请至少选择一个平台账号")
    private List<Integer> markets;
}
