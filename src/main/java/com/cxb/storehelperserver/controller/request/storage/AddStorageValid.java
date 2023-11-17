package com.cxb.storehelperserver.controller.request.storage;

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
public class AddStorageValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Length(min = 18, max = 18, message = "地区码错误")
    private String area;

    @NotEmpty(message = "请输入仓库名称")
    @Length(min = 2, message = "仓库名称长度不能小于2个字符")
    @Length(max = 32, message = "仓库名称长度不能大于32个字符")
    private String name;

    @NotEmpty(message = "请输入仓库地址")
    @Length(min = 4, message = "仓库地址长度不能小于4个字符")
    @Length(max = 32, message = "仓库地址长度不能大于32个字符")
    private String address;
}
