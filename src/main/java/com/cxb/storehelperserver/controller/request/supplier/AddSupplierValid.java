package com.cxb.storehelperserver.controller.request.supplier;

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
public class AddSupplierValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @NotEmpty(message = "请输入供应商名称")
    @Length(min = 4, message = "供应商名称长度不能小于4个字符")
    @Length(max = 32, message = "供应商名称长度不能大于32个字符")
    private String name;

    @NotEmpty(message = "请输入联系人名称")
    @Length(min = 2, message = "联系人名称长度不能小于2个字符")
    @Length(max = 8, message = "联系人名称长度不能大于8个字符")
    private String contact;

    @NotEmpty(message = "请输入手机号")
    @Length(min = 11, max = 11, message = "手机号格式错误")
    private String phone;

    @Length(max = 16, message = "商品备注长度不能大于16个字符")
    private String remark;
}
